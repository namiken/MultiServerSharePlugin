package jp.thelow.chestShare.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.io.Files;

import jp.thelow.chestShare.domain.ShareData;
import jp.thelow.core.Main;

public class ChestSerializeUtil {
  /**
   * 指定したファイルを読み込む。
   *
   * @param file file
   * @return ShareData
   */
  public static ShareData read(File file) {
    YamlConfiguration configuration = new YamlConfiguration();
    try {
      Exception ex = null;
      //最大2回リトライ処理を行う
      for (int i = 0; i < 3; i++) {
        try {
          configuration.load(file);
          return (ShareData) configuration.get("data");
        } catch (IllegalArgumentException e) {
          Main.plugin.getLogger().info("skip:" + e.getMessage());
          return null;
        } catch (Exception e) {
          ex = e;
          //エラーが発生したら300ms待つ
          Thread.sleep(100 * 3);
        }
      }
      throw ex;
    } catch (Exception e) {
      try {
        Main.plugin.getLogger()
            .info(Files.readLines(file, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n")));
        e.printStackTrace();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      return null;
    }
  }

  public static void write(ShareData target, File file) {
    YamlConfiguration yamlConfiguration = new YamlConfiguration();
    yamlConfiguration.set("data", target);
    try {
      yamlConfiguration.save(file);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
