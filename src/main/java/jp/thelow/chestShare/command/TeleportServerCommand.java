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

    if (DataMigrator.isMigrated(p)) {
      if (server.equals("thelow3")) {
        server = "thelow2";
      }
    }

    if (server.equals("thelow0")) {
      server = "thelow3";
    }

    //TPさせる
    return teleportServer(p, server, loc);
  }

  public static boolean teleportServer(Player p, String server, Location loc) {
    //    if (DataMigrator.isMigrated(p)) {
    if (loc == null) {
      loc = p.getLocation();
    }
    return innerTeleportServer(p, server, loc);
    //    } else {
    //      return teleportServerLegacy(p, server, loc);
    //    }
  }

  private static boolean innerTeleportServer(Player p, String server, Location loc) {
    if (Main.getInstance().getServer().getPluginManager().isPluginEnabled("DungeonCore")) {
      TheLowPlayerManager.saveData(p);
    }

    //サーバー移動時にデータを保存しないようにする
    PlayerDataListener.setNoSave(p.getPlayer());
    //行動制限を追加
    PlayerLimitManager.setLimited(p);

    //5秒後に鯖にいる場合はTP失敗と判断する
    TheLowExecutor.executeLater(20 * 5, () -> {
      //行動制限を解除
      PlayerLimitManager.clearLimited(p);
      PlayerDataListener.clearNoSaveOnQuit(p);

      if (!p.isOnline()) { return; }
      p.kickPlayer("サーバー間の移動に失敗しました。");
    });

    TheLowExecutor.executeLater(20, () -> {

      //プレイヤーデータ保存用の処理
      BooleanConsumer callback = ok -> {
        Main.getInstance().getLogger().info("サーバー移動のため、" + p.getName() + "のデータを保存しました。");
        if (!ok) {
          PlayerLimitManager.clearLimited(p);
          p.sendMessage(ChatColor.RED + "プレイヤーデータの保存に失敗したため、サーバー移動出来ません。ダンジョン中の場合は、一旦サーバーから抜けてください");
          return;
        }

        p.sendMessage(ChatColor.GREEN + "別鯖に移動します。");

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

      //プレイヤーデータを保存する
      DatabasePlayerDataSaveLogic.save(p, loc, callback);

    });
    return true;

  }

  //  private static boolean teleportServerLegacy(Player p, String server, Location loc) {
  //    Location beforeLoc = p.getLocation();
  //    if (loc != null) {
  //      p.teleport(loc, TeleportCause.UNKNOWN);
  //    }
  //    //    p.saveData();
  //
  //    if (Main.getInstance().getServer().getPluginManager().isPluginEnabled("DungeonCore")) {
  //      TheLowPlayerManager.saveData(p);
  //    }
  //
  //    //クリックできないようにする
  //    Main.noClickMap.add(p);
  //    p.sendMessage(ChatColor.GREEN + "別鯖に移動します。");
  //
  //    //1秒後に別鯖にTPする
  //    TheLowExecutor.executeLater(1, () -> {
  //      ByteArrayOutputStream b = new ByteArrayOutputStream();
  //      DataOutputStream out = new DataOutputStream(b);
  //      try {
  //        out.writeUTF("Connect");
  //        out.writeUTF(server);
  //      } catch (IOException localIOException) {
  //        localIOException.printStackTrace();
  //      }
  //      p.sendPluginMessage(Main.getInstance(), "BungeeCord", b.toByteArray());
  //    });
  //
  //    //クリック制限を解除
  //    TheLowExecutor.executeLater(20 * 3, () -> {
  //      Main.noClickMap.remove(p);
  //    });
  //
  //    //5秒後に鯖にいる場合はTP失敗と判断する
  //    TheLowExecutor.executeLater(20 * 5, () -> {
  //      if (!p.isOnline()) { return; }
  //      p.teleport(beforeLoc);
  //      p.sendMessage(ChatColor.RED + "サーバー間の移動に失敗しました。");
  //    });
  //    return true;
  //  }

  /**
   * world:x,y,z形式の座標をLocationに変換する。もし不正なフォーマットまたは指定されたワールドが存在しない場合はnullをかえす。
   *
   * @param str world:x,y,z形式の座標
   * @return 解析後の座標
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
