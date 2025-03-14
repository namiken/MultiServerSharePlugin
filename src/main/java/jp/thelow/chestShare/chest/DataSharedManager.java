package jp.thelow.chestShare.chest;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;

import jp.thelow.chestShare.Main;
import jp.thelow.chestShare.Properties;
import jp.thelow.chestShare.command.TeleportServerCommand;
import jp.thelow.chestShare.domain.ChangeBlockData;
import jp.thelow.chestShare.domain.ChestData;
import jp.thelow.chestShare.domain.DoubleChestData;
import jp.thelow.chestShare.domain.ShareData;
import jp.thelow.chestShare.playerdata.PlayerLimitManager;
import jp.thelow.chestShare.util.ChestSerializeUtil;
import jp.thelow.chestShare.util.CommonUtil;

public class DataSharedManager {

  public static ExecutorService writeThread = Executors.newSingleThreadExecutor();

  /**
   * Singleチェストを閉じたときの処理
   *
   * @param chest
   */
  public static void onCloseChest(Chest chest) {
    ChestData chestData = new ChestData();
    chestData.setContents(chest.getBlockInventory().getContents());
    chestData.setChestLocation(chest.getLocation());

    Properties prooerties = Main.getInstance().getProoerties();
    File file = new File(prooerties.getWriteFileDir(),
        Long.toString(System.currentTimeMillis()) + ".txt");
    writeThread.execute(() -> ChestSerializeUtil.write(chestData, file));
  }

  /**
   * Doubleチェストを閉じたときの処理
   *
   * @param chest
   */
  public static void onCloseDoubleChest(DoubleChest chest) {
    DoubleChestData chestData = new DoubleChestData();
    chestData.setContents(chest.getInventory().getContents());
    chestData.setLeftLocation(((Chest) chest.getLeftSide()).getLocation());
    chestData.setRightLocation(((Chest) chest.getRightSide()).getLocation());

    Properties prooerties = Main.getInstance().getProoerties();
    File file = new File(prooerties.getWriteFileDir(),
        Long.toString(System.currentTimeMillis()) + ".txt");
    writeThread.execute(() -> ChestSerializeUtil.write(chestData, file));
  }

  public static void onChangeBlock(Block block) {
    ChangeBlockData changeBlockData = new ChangeBlockData();
    changeBlockData.addBlock(block);

    Properties prooerties = Main.getInstance().getProoerties();
    File file = new File(prooerties.getWriteFileDir(),
        Long.toString(System.currentTimeMillis()) + ".txt");
    writeThread.execute(() -> ChestSerializeUtil.write(changeBlockData, file));
  }

  public static void start() {
    // 1秒に一回ルーチンを行う
    Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(),
        () -> {
          //          //チェスト情報を共有
          //          shareChest();

          //地上ワールドのPlayerをTPさせる
          teleportPlayerOnOverWorld();

          //別鯖からのリクエストにより別鯖にTPさせる
          //          teleportPlayerOnRequest();
        }, 20, 20 * 3);
  }

  protected static void teleportPlayerOnOverWorld() {
    if (!Main.getInstance().getProoerties().isAutoMoveServer()) { return; }

    Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
    for (Player player : onlinePlayers) {
      if (player.getWorld().getName().contains("dungeon")) {
        continue;
      }

      if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
        continue;
      }

      //行動制限中の場合はスキップ
      if (PlayerLimitManager.isLimited(player)) {
        continue;
      }

      Location location = player.getLocation();
      //ダンジョンワールド以外にいる場合はTPする
      CommonUtil.execSync(() -> {
        TeleportServerCommand.teleportServer(player,
            Main.getInstance().getProoerties().getOverworldServer(), null);
        Main.getInstance().getLogger().info(
            player.getName() + "をメインサーバーにTPさせました。" + location.getWorld() + ":" + location.getBlockX() + ","
                + location.getBlockY() + "," + location.getBlockZ());
      });
    }
  }

  protected static void shareChest() {
    //Fileをデータに変換
    String readFileDir = Main.getInstance().getProoerties().getReadFileDir();
    File file = new File(readFileDir);
    File[] listFiles = file.listFiles();
    if (listFiles == null) {
      listFiles = new File[0];
    }
    List<ShareData> shareDatas = Stream.of(listFiles).sorted((f1, f2) -> f1.getName().compareTo(f2.getName()))
        .filter(f -> f.isFile()).map(f -> ChestSerializeUtil.read(f)).filter(Objects::nonNull)
        .collect(Collectors.toList());
    //ファイルを削除
    Arrays.stream(listFiles).forEach(f -> f.delete());

    if (!shareDatas.isEmpty()) {
      CommonUtil.execSync(() -> shareDatas.forEach(s -> {
        try {
          s.applyServer();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }));
    }
  }

  public static void close() {
    writeThread.shutdown();
  }
}
