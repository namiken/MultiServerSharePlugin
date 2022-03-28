package jp.thelow.chestShare.listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import jp.thelow.chestShare.Main;
import jp.thelow.chestShare.playerdata.DataMigrator;
import jp.thelow.chestShare.playerdata.DatabasePlayerDataSaveLogic;
import jp.thelow.chestShare.playerdata.PlayerDatData;
import jp.thelow.chestShare.playerdata.PlayerLimitManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

public class PlayerDataListener implements Listener {

  private static Map<UUID, String> noSavePlayer = new ConcurrentHashMap<>();

  private static Map<UUID, LoadPlayerData> loadPlayerDataMap = new ConcurrentHashMap<>();

  @EventHandler
  public void onAsyncPlayerJoinEvent(AsyncPlayerPreLoginEvent e) {

    if (!DataMigrator.isMigrated(e.getUniqueId())) { return; }

    //プレイヤーデータを同期処理でロードする
    try {
      PlayerDatData playerDatData = DatabasePlayerDataSaveLogic.syncLoad(e.getUniqueId());
      if (playerDatData != null) {
        loadPlayerDataMap.put(e.getUniqueId(), new LoadPlayerData(playerDatData, playerDatData.toNbtCompound()));
      }
    } catch (Exception ex) {
      e.disallow(Result.KICK_OTHER, "プレイヤーデータのロードに失敗したため、ログイン出来ませんでした。もう一度やり直してください。");
      throw new RuntimeException(ex);
    }
  }

  @EventHandler
  public void onPlayerJoinEvent(PlayerJoinEvent e) {

    Player player = e.getPlayer();
    if (!DataMigrator.isMigrated(player)) { return; }

    LoadPlayerData playerData = loadPlayerDataMap.remove(player.getUniqueId());
    if (playerData == null) {
      Main.getInstance().getLogger().info("PlayerDatデータが存在しないため、適用をスキップしました。");
      return;
    }

    DatabasePlayerDataSaveLogic.load(player, playerData.getNbtTagCompound(), playerData.getPlayerDatData());
    player.sendMessage(ChatColor.GREEN + "インベントリ・座標情報のロードに成功しました。");

    //行動制限を削除
    clearNoSaveOnQuit(e.getPlayer());
  }

  public static void setNoSave(Player p) {
    noSavePlayer.put(p.getUniqueId(), "");
  }

  public static void clearNoSaveOnQuit(Player p) {
    noSavePlayer.remove(p.getUniqueId());
  }

  public static boolean isNoSave(Player p) {
    return noSavePlayer.containsKey(p.getUniqueId());
  }

  @EventHandler
  public void onPlayerQuitEvent(PlayerQuitEvent e) {
    PlayerLimitManager.clearLimited(e.getPlayer());
    DatabasePlayerDataSaveLogic.onQuit(e.getPlayer());

    if (!noSavePlayer.remove(e.getPlayer().getUniqueId(), "")) {
      DatabasePlayerDataSaveLogic.save(e.getPlayer());
    }
  }

  @AllArgsConstructor
  @Data
  class LoadPlayerData {
    private PlayerDatData playerDatData;

    private NBTTagCompound nbtTagCompound;
  }
}
