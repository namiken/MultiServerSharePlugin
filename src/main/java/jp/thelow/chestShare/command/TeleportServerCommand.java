package jp.thelow.chestShare.command;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import jp.thelow.chestShare.Main;
import jp.thelow.chestShare.listener.PlayerDataListener;
import jp.thelow.chestShare.playerdata.DataMigrator;
import jp.thelow.chestShare.playerdata.DatabasePlayerDataSaveLogic;
import jp.thelow.chestShare.playerdata.PlayerLimitManager;
import jp.thelow.chestShare.util.BooleanConsumer;
import jp.thelow.chestShare.util.TheLowExecutor;
import jp.thelow.dungeon.api.player.TheLowPlayerManager;

public class TeleportServerCommand implements CommandExecutor {

  @SuppressWarnings("deprecation")
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    Player p = null;
    String server = null;
    Location loc = null;

    switch (args.length) {
    case 0:
      return false;
    case 3:
      loc = getLocationByString(args[2]);
    case 2:
      p = Bukkit.getPlayerExact(args[1]);
    case 1:
      server = args[0];
      break;
    default:
      return false;
    }

    if (p == null) {
      p = (Player) sender;
    }

    //TPããã
    return teleportServer(p, server, loc);
  }

  public static boolean teleportServer(Player p, String server, Location loc) {
    if (DataMigrator.isMigrated(p)) {
      if (loc == null) {
        loc = p.getLocation();
      }
      return innerTeleportServer(p, server, loc);
    } else {
      return teleportServerLegacy(p, server, loc);
    }
  }

  private static boolean innerTeleportServer(Player p, String server, Location loc) {
    if (Main.getInstance().getServer().getPluginManager().isPluginEnabled("DungeonCore")) {
      TheLowPlayerManager.saveData(p);
    }

    //ãµã¼ãã¼ç§»åæã«ãã¼ã¿ãä¿å­ããªãããã«ãã
    PlayerDataListener.setNoSave(p.getPlayer());

    //5ç§å¾ã«é¯ã«ããå ´åã¯TPå¤±æã¨å¤æ­ãã
    TheLowExecutor.executeLater(20 * 5, () -> {
      //è¡åå¶éãè§£é¤
      PlayerLimitManager.clearLimited(p);
      PlayerDataListener.clearNoSaveOnQuit(p);

      if (!p.isOnline()) { return; }
      p.sendMessage(ChatColor.RED + "ãµã¼ãã¼éã®ç§»åã«å¤±æãã¾ããã");
    });

    TheLowExecutor.executeLater(20, () -> {

      PlayerLimitManager.setLimited(p);
      //ãã¬ã¤ã¤ã¼ãã¼ã¿ä¿å­ç¨ã®å¦ç
      BooleanConsumer callback = ok -> {
        if (!ok) {
          PlayerLimitManager.clearLimited(p);
          p.sendMessage(ChatColor.RED + "ãã¬ã¤ã¤ã¼ãã¼ã¿ã®ä¿å­ã«å¤±æããããããµã¼ãã¼ç§»ååºæ¥ã¾ããããã³ã¸ã§ã³ä¸­ã®å ´åã¯ãä¸æ¦ãµã¼ãã¼ããæãã¦ãã ãã");
          return;
        }

        p.sendMessage(ChatColor.GREEN + "å¥é¯ã«ç§»åãã¾ãã");

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
          out.writeUTF("Connect");
          out.writeUTF(server);
        } catch (IOException localIOException) {
          localIOException.printStackTrace();
        }
        p.sendPluginMessage(Main.getInstance(), "BungeeCord", b.toByteArray());
      };

      //ãã¬ã¤ã¤ã¼ãã¼ã¿ãä¿å­ãã
      DatabasePlayerDataSaveLogic.save(p, loc, callback);

    });
    return true;

  }

  private static boolean teleportServerLegacy(Player p, String server, Location loc) {
    Location beforeLoc = p.getLocation();
    if (loc != null) {
      p.teleport(loc, TeleportCause.UNKNOWN);
    }
    //    p.saveData();

    if (Main.getInstance().getServer().getPluginManager().isPluginEnabled("DungeonCore")) {
      TheLowPlayerManager.saveData(p);
    }

    //ã¯ãªãã¯ã§ããªãããã«ãã
    Main.noClickMap.add(p);
    p.sendMessage(ChatColor.GREEN + "å¥é¯ã«ç§»åãã¾ãã");

    //1ç§å¾ã«å¥é¯ã«TPãã
    TheLowExecutor.executeLater(1, () -> {
      ByteArrayOutputStream b = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(b);
      try {
        out.writeUTF("Connect");
        out.writeUTF(server);
      } catch (IOException localIOException) {
        localIOException.printStackTrace();
      }
      p.sendPluginMessage(Main.getInstance(), "BungeeCord", b.toByteArray());
    });

    //ã¯ãªãã¯å¶éãè§£é¤
    TheLowExecutor.executeLater(20 * 3, () -> {
      Main.noClickMap.remove(p);
    });

    //5ç§å¾ã«é¯ã«ããå ´åã¯TPå¤±æã¨å¤æ­ãã
    TheLowExecutor.executeLater(20 * 5, () -> {
      if (!p.isOnline()) { return; }
      p.teleport(beforeLoc);
      p.sendMessage(ChatColor.RED + "ãµã¼ãã¼éã®ç§»åã«å¤±æãã¾ããã");
    });
    return true;
  }

  /**
   * world:x,y,zå½¢å¼ã®åº§æ¨ãLocationã«å¤æãããããä¸æ­£ãªãã©ã¼ãããã¾ãã¯æå®ãããã¯ã¼ã«ããå­å¨ããªãå ´åã¯nullããããã
   *
   * @param str world:x,y,zå½¢å¼ã®åº§æ¨
   * @return è§£æå¾ã®åº§æ¨
   */
  public static Location getLocationByString(String str) {
    if (str == null || str.isEmpty()) { return null; }
    try {
      String[] split = str.split(":");
      World w = Bukkit.getWorld(split[0]);

      String[] split2 = split[1].split(",");
      double x = Double.parseDouble(split2[0]);
      double y = Double.parseDouble(split2[1]);
      double z = Double.parseDouble(split2[2]);

      if (w == null) { return null; }

      return new Location(w, x, y, z);
    } catch (Exception e) {
      return null;
    }
  }
}
