package jp.thelow.chestShare.playerdata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import jp.thelow.chestShare.Main;
import jp.thelow.chestShare.dao.PlayerDatDataDao;
import jp.thelow.chestShare.util.BooleanConsumer;
import jp.thelow.chestShare.util.TheLowExecutor;
import jp.thelow.thelowSql.database.ConnectionFactory;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IInventory;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;

public class DatabasePlayerDataSaveLogic {

  private static Map<UUID, String> beforeDataMap = new HashMap<>();

  private static PlayerDatDataDao playerDatDataDao = new PlayerDatDataDao();

  public static void save(Player p) {
    save(p, p.getLocation());
  }

  public static void save(Player p, Location loc) {
    save(p, loc, b -> {
    });
  }

  public static void saveSyncAllPlayer() {
    try {
      Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
      for (Player p : onlinePlayers) {

        try {
          EntityPlayer entityhuman = ((CraftPlayer) p).getHandle();
          PlayerDatData playerDatData = PlayerDatData.fromEntity(p, p.getLocation());

          NBTTagCompound nbttagcompound = new NBTTagCompound();
          entityhuman.e(nbttagcompound);
          String nbtag = setNbttag(nbttagcompound);
          if (nbtag == null) { return; }
          playerDatData.setDatData(nbtag);

          playerDatDataDao.upsertSync(playerDatData);
        } catch (Exception e) {
          Main.getInstance().getLogger().log(Level.FINE, "プレイヤーデータの保存に失敗しました。", e);
        }
      }
    } finally {
      ConnectionFactory.safeClose();
    }
  }

  public static void save(Player p, Location loc, BooleanConsumer callback) {
    if (!p.isOnline()) {
      callback.accept(true);
      return;
    }

    EntityPlayer entityhuman = ((CraftPlayer) p).getHandle();
    PlayerDatData playerDatData = PlayerDatData.fromEntity(p, loc);

    NBTTagCompound nbttagcompound = new NBTTagCompound();
    entityhuman.e(nbttagcompound);

    TheLowExecutor.execAsync(() -> {
      // 保管データを文字列にする
      return setNbttag(nbttagcompound);
    }, e -> {
      if (e == null) {
        Main.getInstance().getLogger().warning("プレイヤーデータの保存に失敗しました。");
        callback.accept(false);
        return;
      }

      String beforeData = beforeDataMap.get(p.getUniqueId());
      if (e.equals(beforeData)) {
        //データ内容が前回と同じ場合は何もしない
        callback.accept(true);
        return;
      }

      playerDatData.setDatData(e);

      beforeDataMap.put(p.getUniqueId(), e);
      playerDatDataDao.upsert(playerDatData, callback);

    });
  }

  private static String setNbttag(NBTTagCompound nbttagcompound) {
    try {
      ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
      NBTCompressedStreamTools.a(nbttagcompound, outputstream);
      Encoder encoder = Base64.getEncoder();
      return encoder.encodeToString(outputstream.toByteArray());
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void openInv(UUID dataPlayer, Player applyPlayer, Consumer<PlayerDataLoadResult> callback) {
    @SuppressWarnings("deprecation")
    Consumer<PlayerDatData> syncFunction = entity -> {
      if (entity == null) {
        callback.accept(PlayerDataLoadResult.NOT_FOUND);
        return;
      }

      TheLowExecutor.execAsync(() -> {
        return entity.toNbtCompoundIgnoreException();
      }, nbt -> {
        if (nbt == null) {
          callback.accept(PlayerDataLoadResult.BREAK_NBTTAG);
          return;
        }
        NBTTagList nbttaglist = nbt.getList("Inventory", 10);

        Inventory inventory = Bukkit.createInventory(null, 9 * 10);
        IInventory bukkitInventory = ((CraftInventory) inventory).getInventory();

        for (int i = 0; i < nbttaglist.size(); ++i) {

          NBTTagCompound nbttagcompound = nbttaglist.get(i);
          int j = nbttagcompound.getByte("Slot") & 255;
          ItemStack itemstack = ItemStack.createStack(nbttagcompound);

          //プレイヤーインベントリ
          if (j >= 0 && j < 36) {
            bukkitInventory.setItem(j, itemstack);
          }

          //装備欄
          if (j >= 100 && j < 4 + 100) {
            bukkitInventory.setItem(j - 100 + 5 * 9, itemstack);
          }
        }

        NBTTagList nbttaglistEnderChest = nbt.getList("EnderItems", 10);
        //エンダーチェスト
        for (int i = 0; i < nbttaglistEnderChest.size(); ++i) {
          NBTTagCompound nbttagcompound = nbttaglistEnderChest.get(i);
          ItemStack itemstack = ItemStack.createStack(nbttagcompound);
          int j = nbttagcompound.getByte("Slot") & 255;
          bukkitInventory.setItem(7 * 9 + j, itemstack);
        }

        for (int i = 4 * 9; i < 5 * 9; ++i) {
          org.bukkit.inventory.ItemStack panel = new org.bukkit.inventory.ItemStack(Material.STAINED_GLASS_PANE);
          panel.getData().setData((byte) 1);
          panel.setDurability((byte) 1);
          ItemMeta itemMeta = panel.getItemMeta();
          itemMeta.setDisplayName("ここより上はインベントリ");
          panel.setItemMeta(itemMeta);
          inventory.setItem(i, panel);
        }

        for (int i = 6 * 9; i < 7 * 9; ++i) {
          org.bukkit.inventory.ItemStack panel = new org.bukkit.inventory.ItemStack(Material.STAINED_GLASS_PANE);
          panel.getData().setData((byte) 1);
          panel.setDurability((byte) 1);
          ItemMeta itemMeta = panel.getItemMeta();
          itemMeta.setDisplayName("ここより下はエンダーチェスト");
          panel.setItemMeta(itemMeta);
          inventory.setItem(i, panel);
        }

        applyPlayer.openInventory(inventory);

        callback.accept(PlayerDataLoadResult.SUCCESS);
      });
    };

    playerDatDataDao.select(dataPlayer, syncFunction);
  }

  public static PlayerDatData syncLoad(UUID dataPlayer) {
    return playerDatDataDao.syncSelect(dataPlayer);
  }

  @SuppressWarnings("deprecation")
  public static void load(Player applyPlayer, NBTTagCompound nbt, PlayerDatData entity) {
    //保存したデータを適用させる
    EntityPlayer entityhuman = ((CraftPlayer) applyPlayer).getHandle();
    World world = entityhuman.getWorld();
    //ワールド間TPをさせるため、スポーン時のワールドは「world」にする
    nbt.setString("world", world.getWorld().getName());
    nbt.remove("WorldUUIDMost");
    nbt.remove("WorldUUIDLeast");

    entityhuman.f(nbt);

    Location location = entity.getPlayerLocation();
    applyPlayer.teleport(location);

    //TPがうまく行かないことがあるので念のため時間差でもTPする
    TheLowExecutor.executeLater(3, () -> {
      applyPlayer.teleport(location);
    });

    applyPlayer.setHealth(Math.min(applyPlayer.getMaxHealth(), entity.getHp()));
    applyPlayer.setLevel(entity.getMp());

    //最大値の適用が遅れる可能性があるので２秒後に適用する
    TheLowExecutor.executeLater(20 * 2, () -> {
      applyPlayer.setHealth(Math.min(applyPlayer.getMaxHealth(), entity.getHp()));
      applyPlayer.setLevel(entity.getMp());
    });

    applyPlayer.updateInventory();

    forceApplyGamemode(applyPlayer);
  }

  public static void loadAsync(UUID dataPlayer, Player applyPlayer, Consumer<PlayerDataLoadResult> callback) {

    Consumer<PlayerDatData> syncFunction = entity -> {
      if (entity == null) {
        callback.accept(PlayerDataLoadResult.NOT_FOUND);
        return;
      }

      TheLowExecutor.execAsync(() -> {
        return entity.toNbtCompoundIgnoreException();
      }, nbt -> {
        if (nbt == null) {
          callback.accept(PlayerDataLoadResult.BREAK_NBTTAG);
          return;
        }

        load(applyPlayer, nbt, entity);
        callback.accept(PlayerDataLoadResult.SUCCESS);
      });
    };

    playerDatDataDao.select(dataPlayer, syncFunction);
  }

  @SuppressWarnings("deprecation")
  public static void forceApplyGamemode(Player applyPlayer) {
    GameMode mode = applyPlayer.getGameMode();
    PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(applyPlayer, mode);
    CraftPlayer craftPlayer = (CraftPlayer) applyPlayer;
    Bukkit.getServer().getPluginManager().callEvent(event);
    if (event.isCancelled()) { return; }

    craftPlayer.getHandle().setSpectatorTarget(craftPlayer.getHandle());
    craftPlayer.getHandle().playerInteractManager.setGameMode(EnumGamemode.getById(mode.getValue()));
    craftPlayer.getHandle().fallDistance = 0.0F;
    craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(3, mode.getValue()));
  }

  public static void onQuit(Player p) {
    beforeDataMap.remove(p.getUniqueId());
  }
}
