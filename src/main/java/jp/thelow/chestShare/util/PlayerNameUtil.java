package jp.thelow.chestShare.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerNameUtil {

  private static Map<String, UUID> map = new HashMap<>();

  public static void init() {
    OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
    for (OfflinePlayer offlinePlayer : offlinePlayers) {
      if (offlinePlayer == null) {
        continue;
      }
      if (offlinePlayer.getName() == null) {
        continue;
      }
      map.put(offlinePlayer.getName().toLowerCase(), offlinePlayer.getUniqueId());
    }

  }

  public static OfflinePlayer byName(String playerName) {
    @SuppressWarnings("deprecation")
    Player playerExact = Bukkit.getPlayerExact(playerName);
    if (playerExact != null) { return playerExact; }

    UUID uuid = map.get(playerName.toLowerCase());

    if (uuid == null) {
      try {
        uuid = UUID.fromString(playerName);
      } catch (IllegalArgumentException e) {
        // ignore
      }
    }

    if (uuid != null) { return Bukkit.getOfflinePlayer(uuid); }
    return null;
  }
}
