package jp.thelow.chestShare.playerdata;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

public class PlayerLimitManager {
  private static Set<UUID> limitedSet = new HashSet<UUID>();

  public static boolean isLimited(Player p) {
    return limitedSet.contains(p.getUniqueId());
  }

  public static void setLimited(Player p) {
    limitedSet.add(p.getUniqueId());
  }

  public static void clearLimited(Player p) {
    limitedSet.remove(p.getUniqueId());
  }
}
