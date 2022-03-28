package jp.thelow.chestShare.playerdata;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class PlayerLimitManager {
  private static Set<UUID> limitedSet = new HashSet<UUID>();

  public static boolean isLimited(HumanEntity p) {
    return limitedSet.contains(p.getUniqueId());
  }

  public static void setLimited(Player p) {
    setLimited(p.getUniqueId());
  }

  public static void setLimited(UUID uuid) {
    limitedSet.add(uuid);
  }

  public static void clearLimited(Player p) {
    limitedSet.remove(p.getUniqueId());
  }
}
