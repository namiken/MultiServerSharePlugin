package jp.thelow.chestShare.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.io.Files;

import jp.thelow.chestShare.domain.ShareData;

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
      configuration.load(file);
      return (ShareData) configuration.get("data");
    } catch (Exception e) {
      try {
        System.out.println(Files.readLines(file, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n")));
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
