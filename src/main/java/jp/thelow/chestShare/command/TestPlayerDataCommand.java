package jp.thelow.chestShare.command;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;

import jp.thelow.chestShare.Main;
import jp.thelow.chestShare.playerdata.DatabasePlayerDataSaveLogic;
import jp.thelow.chestShare.playerdata.PlayerDataLoadResult;
import jp.thelow.chestShare.util.PlayerNameUtil;
import jp.thelow.chestShare.util.TheLowExecutor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;

public class TestPlayerDataCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED + "プレイヤーのみ使用可能です。");
      return true;
    }
    Player p = (Player) sender;

    if (args[0].equals("load")) {
      OfflinePlayer dataPlayer = getOfflinePlayerByIdentify(args[1]);
      if (dataPlayer == null) {
        sender.sendMessage(ChatColor.RED + "指定したプレイヤーは存在しません。");
        return true;
      }

      DatabasePlayerDataSaveLogic.loadAsync(dataPlayer.getUniqueId(), p, r -> {
        if (r == PlayerDataLoadResult.BREAK_NBTTAG) {
          sender.sendMessage(ChatColor.RED + "指定したプレイヤーのデータは破損しています。");
        } else if (r == PlayerDataLoadResult.NOT_FOUND) {
          sender.sendMessage(ChatColor.RED + "指定したプレイヤーのデータはデータベース上に存在しません。");
        } else {
          sender.sendMessage(ChatColor.GREEN + "データの適用が完了しました。");
        }
      });

    } else if (args[0].equals("loadFile")) {
      File file = new File(args[1]);
      if (!file.isFile()) {
        sender.sendMessage(ChatColor.RED + "指定したファイルが存在しません。");
        return true;
      }
      TheLowExecutor.execAsync(() -> {
        try (
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(args[1])),
                StandardCharsets.UTF_8);) {
          ZipEntry zipentry = zis.getNextEntry();
          if (zipentry == null) {
            Main.getInstance().getLogger().warning("Zipファイル内にファイルが存在しません。");
            return null;
          }

          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          byte[] data = new byte[1024];
          int count = 0;
          while ((count = zis.read(data)) != -1) {
            bos.write(data, 0, count);
          }

          JsonPlayerDatData fromJson = new Gson().fromJson(new String(bos.toByteArray(), StandardCharsets.UTF_8),
              JsonPlayerDatData.class);

          NBTTagCompound nbtCompound = fromJson.toNbtCompound();

          NBTTagList inv = (NBTTagList) nbtCompound.get("Inventory");

          ArrayList<ItemStack> list = new ArrayList<ItemStack>();
          for (int i = 0; i < inv.size(); i++) {
            net.minecraft.server.v1_8_R3.ItemStack itemStack = net.minecraft.server.v1_8_R3.ItemStack
                .createStack(inv.get(i));
            list.add(CraftItemStack.asBukkitCopy(itemStack));
          }

          return list;

        } catch (IOException e) {
          e.printStackTrace();
          return null;
        }
      }, entity -> {
        Inventory inventory = Bukkit.createInventory(null, entity.size());
        for (int i = 0; i < entity.size(); i++) {
          ItemStack itemStack = entity.get(i);
          inventory.setItem(i, itemStack);
        }
        ((Player) sender).openInventory(inventory);
      });
    } else if (args[0].equals("save")) {
      OfflinePlayer dataPlayer = getOfflinePlayerByIdentify(args[1]);
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
      OfflinePlayer dataPlayer = getOfflinePlayerByIdentify(args[1]);
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

  public static OfflinePlayer getOfflinePlayerByIdentify(String identify) {
    UUID uuid = (UUID) TheLowExecutor.getObjectIgnoreException(() -> {
      return UUID.fromString(identify);
    }, (Object) null);
    return uuid != null ? Bukkit.getOfflinePlayer(uuid) : PlayerNameUtil.byName(identify);
  }

}
