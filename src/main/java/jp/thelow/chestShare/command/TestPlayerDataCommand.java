package jp.thelow.chestShare.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jp.thelow.chestShare.playerdata.DatabasePlayerDataSaveLogic;
import jp.thelow.chestShare.playerdata.PlayerDataLoadResult;
import jp.thelow.dungeon.util.MinecraftUtil;

public class TestPlayerDataCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED + "プレイヤーのみ使用可能です。");
      return true;
    }
    Player p = (Player) sender;

    if (args[0].equals("load")) {
      OfflinePlayer dataPlayer = MinecraftUtil.getOfflinePlayerByIdentify(args[1]);
      if (dataPlayer == null) {
        sender.sendMessage(ChatColor.RED + "指定したプレイヤーは存在しません。");
        return true;
      }

      DatabasePlayerDataSaveLogic.load(dataPlayer.getUniqueId(), p, r -> {
        if (r == PlayerDataLoadResult.BREAK_NBTTAG) {
          sender.sendMessage(ChatColor.RED + "指定したプレイヤーのデータは破損しています。");
        } else if (r == PlayerDataLoadResult.NOT_FOUND) {
          sender.sendMessage(ChatColor.RED + "指定したプレイヤーのデータはデータベース上に存在しません。");
        } else {
          sender.sendMessage(ChatColor.GREEN + "データの適用が完了しました。");
        }
      });

    } else if (args[0].equals("save")) {
      OfflinePlayer dataPlayer = MinecraftUtil.getOfflinePlayerByIdentify(args[1]);
      if (dataPlayer == null) {
        sender.sendMessage(ChatColor.RED + "指定したプレイヤーは存在しません。");
        return true;
      }
      Player player = Bukkit.getPlayer(dataPlayer.getUniqueId());
      if (player == null) {
        sender.sendMessage(ChatColor.RED + "指定したプレイヤーはオフラインです。");
        return true;
      }
      DatabasePlayerDataSaveLogic.save(player);
    } else if (args[0].equalsIgnoreCase("openInv")) {
      OfflinePlayer dataPlayer = MinecraftUtil.getOfflinePlayerByIdentify(args[1]);
      if (dataPlayer == null) {
        sender.sendMessage(ChatColor.RED + "指定したプレイヤーは存在しません。");
        return true;
      }

      DatabasePlayerDataSaveLogic.openInv(dataPlayer.getUniqueId(), p, r -> {
        if (r == PlayerDataLoadResult.BREAK_NBTTAG) {
          sender.sendMessage(ChatColor.RED + "指定したプレイヤーのデータは破損しています。");
        } else if (r == PlayerDataLoadResult.NOT_FOUND) {
          sender.sendMessage(ChatColor.RED + "指定したプレイヤーのデータはデータベース上に存在しません。");
        }
      });
    }
    return true;
  }
}
