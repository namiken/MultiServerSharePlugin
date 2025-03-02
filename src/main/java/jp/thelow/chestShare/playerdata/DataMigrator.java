package jp.thelow.chestShare.playerdata;

import java.util.UUID;

import org.bukkit.entity.Player;

import jp.thelow.chestShare.Main;

public class DataMigrator {

  public static boolean isMigrated(Player p) {
    return isMigrated(p.getUniqueId());
    //    return true;
  }

  public static boolean isMigrated(UUID uuid) {
    return Main.getInstance().getProoerties().getAllowUuid().contains(uuid);
  }
}
