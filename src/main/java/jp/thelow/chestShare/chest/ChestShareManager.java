package jp.thelow.chestShare.chest;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;

import jp.thelow.chestShare.Main;
import jp.thelow.chestShare.Properties;
import jp.thelow.chestShare.domain.ChestData;
import jp.thelow.chestShare.domain.DoubleChestData;
import jp.thelow.chestShare.domain.ShareData;
import jp.thelow.chestShare.file.ChestSerializeUtil;

public class ChestShareManager {

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

  public static void start() {
    // 1秒に一回ルーチンを行う
    Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(),
        () -> {
          //Fileをデータに変換
          String readFileDir = Main.getInstance().getProoerties().getReadFileDir();
          File file = new File(readFileDir);
          File[] listFiles = file.listFiles();
          List<ShareData> shareDatas = Stream.of(listFiles).sorted((f1, f2) -> f1.getName().compareTo(f2.getName()))
              .map(f -> ChestSerializeUtil.read(f)).filter(Objects::nonNull).collect(Collectors.toList());
          //ファイルを削除
          Arrays.stream(listFiles).forEach(f -> f.delete());

          if (!shareDatas.isEmpty()) {
            Bukkit.getScheduler().callSyncMethod(Main.getInstance(), () -> {
              shareDatas.forEach(s -> s.applyServer());
              return null;
            });
          }
        }, 20, 20);
  }

  public static void close() {
    writeThread.shutdown();
  }
}
