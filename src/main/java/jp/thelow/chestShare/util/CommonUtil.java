package jp.thelow.chestShare.util;

import java.text.MessageFormat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import jp.thelow.chestShare.Main;

public class CommonUtil {

  public static void execSync(Runnable runnable) {
    Bukkit.getScheduler().callSyncMethod(Main.getInstance(), () -> {
      try {
        runnable.run();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    });
  }

  /**
   * world:x,y,z形式でLocationを文字列に変換する。
   *
   * @param loc 変換前のLocation
   * @return 変換後の文字列
   */
  public static String getLocationString3(Location loc) {
    if (loc == null) { return "null"; }
    return MessageFormat.format("{0}:{1},{2},{3}", loc.getWorld().getName(), Integer.toString(loc.getBlockX()),
        Integer.toString(loc.getBlockY()),
        Integer.toString(loc.getBlockZ()));
  }

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
