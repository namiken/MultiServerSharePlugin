package jp.thelow.chestShare.playerdata;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableSet;

public class DataMigrator {

  private static ImmutableSet<UUID> migrated = ImmutableSet.of(UUID.fromString("67d7d0a0-2e5a-498c-b74b-ea72e0b10b3d"),
      UUID.fromString("d5aa162a-954a-3faa-8a8c-a9775682486d"));

  public static boolean isMigrated(Player p) {
    return migrated.contains(p.getUniqueId());
  }

  public static boolean isMigrated(UUID uuid) {
    return migrated.contains(uuid);
  }
}
