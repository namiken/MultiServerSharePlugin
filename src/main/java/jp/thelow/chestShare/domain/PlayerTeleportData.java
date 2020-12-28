package jp.thelow.chestShare.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import jp.thelow.chestShare.command.TeleportServerCommand;

public class PlayerTeleportData implements ConfigurationSerializable, ShareData {

  private Location loc;

  private String serverName;

  private UUID uuid;

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("location", loc);
    dataMap.put("serverName", serverName);
    dataMap.put("uuid", uuid.toString());
    return dataMap;
  }

  public static PlayerTeleportData deserialize(Map<String, Object> map) {

    //オブジェクトを作る
    PlayerTeleportData data = new PlayerTeleportData();
    data.loc = (Location) map.get("chestLocation");
    data.serverName = (String) map.get("serverName");
    data.uuid = UUID.fromString((String) map.get("uuid"));

    return data;
  }

  public boolean isOnline() {
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
    return offlinePlayer.isOnline();
  }

  @Override
  public void applyServer() {
    Player player = Bukkit.getPlayer(uuid);
    if (player != null && player.isOnline()) {
      TeleportServerCommand.teleportServer(player, serverName, loc);
    }
  }

  public Location getLoc() {
    return loc;
  }

  public void setLoc(Location loc) {
    this.loc = loc;
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

}
