package jp.thelow.chestShare.util;

import org.bukkit.Bukkit;

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
}
