package jp.thelow.chestShare.command;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.gson.Gson;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;

public class TestDebugger {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("使用方法: <親ディレクトリ> <基準日付(yyyymmdd-hhmmss)>");
      return;
    }

    System.out.println("開始します。:" + args[1]);

    File baseDir = new File(args[0]); // 親ディレクトリ
    if (!baseDir.exists() || !baseDir.isDirectory()) {
      System.out.println("ディレクトリが存在しません");
      return;
    }

    // 指定された基準日付を `Date` に変換
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    Date thresholdDate;
    try {
      thresholdDate = dateFormat.parse(args[1]);
    } catch (ParseException e) {
      System.out.println("日付の形式が正しくありません: " + args[1]);
      return;
    }

    // 親ディレクトリ配下のフォルダを取得
    File[] subFolders = baseDir.listFiles(File::isDirectory);
    if (subFolders == null || subFolders.length == 0) {
      System.out.println("サブフォルダがありません");
      return;
    }

    int totalFolder = subFolders.length;

    int count = 0;
    for (File folder : subFolders) {
      if (count % 100 == 0) {
        System.out.println(100 * count / totalFolder + "%");
      }
      count++;

      // 各フォルダ内のZIPファイルを取得
      Arrays.stream(folder.listFiles((dir, name) -> name.matches("\\d{8}-\\d{6}\\.zip")))
          .map(zip -> new AbstractMap.SimpleEntry<>(zip, getDateFromFileName(zip.getName(), dateFormat)))
          .filter(entry -> entry.getValue() != null && entry.getValue().after(thresholdDate)) // 指定日付より新しいもののみ
          .max(Comparator.comparing(AbstractMap.SimpleEntry::getValue))
          .ifPresent(entry -> {
            try {
              execute(entry.getKey());
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
    }
  }

  /**
   * ZIPファイルの名前 (yyyymmdd-hhmmss.zip) から Date を取得
   */
  private static Date getDateFromFileName(String fileName, SimpleDateFormat dateFormat) {
    try {
      return dateFormat.parse(fileName.substring(0, 15)); // "yyyymmdd-hhmmss"
    } catch (ParseException e) {
      return null;
    }
  }

  public static void execute(File zip) throws Exception {
    try (ZipInputStream zis = new ZipInputStream(
        new BufferedInputStream(new FileInputStream(zip)),
        StandardCharsets.UTF_8)) {
      ZipEntry zipentry = zis.getNextEntry();
      if (zipentry == null) {
        System.out.println("Zipファイル内にファイルが存在しません。");
        return;
      }

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] data = new byte[1024];
      int count;
      while ((count = zis.read(data)) != -1) {
        bos.write(data, 0, count);
      }

      JsonPlayerDatData fromJson = new Gson().fromJson(new String(bos.toByteArray(), StandardCharsets.UTF_8),
          JsonPlayerDatData.class);

      NBTTagCompound nbtCompound = fromJson.toNbtCompound();
      String mcid = nbtCompound.getCompound("bukkit").getString("lastKnownName");

      // アイテムのスロット数カウント（個数ではなくスロット数）
      Map<String, Integer> itemSlotCount = new HashMap<>();
      NBTTagList inv = (NBTTagList) nbtCompound.get("Inventory");

      for (int i = 0; i < inv.size(); i++) {
        NBTTagCompound nbtTagCompound = inv.get(i);
        String id = nbtTagCompound.getCompound("tag").getString("thelow_item_id");

        if (id != null && !id.isEmpty()) {
          // スロット単位でカウント
          itemSlotCount.compute(id, (key, value) -> value == null ? 1 : value + 1);
        }
      }

      // 20スロット以上あるアイテムが存在するかチェック
      boolean hasItemWith20Slots = itemSlotCount.values().stream().anyMatch(slotCount -> slotCount >= 20);

      if (hasItemWith20Slots) {
        System.out.println(mcid + "\t" + fromJson.getUpdateAt() + "\t" + itemSlotCount);
        System.out.println(zip.getAbsolutePath());
      }
    }
  }
}
