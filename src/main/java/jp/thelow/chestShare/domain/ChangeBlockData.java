package jp.thelow.chestShare.domain;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import jp.thelow.chestShare.Main;

public class ChangeBlockData implements ConfigurationSerializable, ShareData {

  private String type;
  private int data;
  private Location location;

  @SuppressWarnings("deprecation")
  public void addBlock(Block block) {
    type = block.getType().name();
    data = block.getData();
    location = block.getLocation();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void applyServer() {
    Block block = location.getBlock();
    block.setData((byte) data);
    block.setType(Material.valueOf(type));
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("type", type);
    dataMap.put("data", data);
    dataMap.put("location", location);
    return dataMap;
  }

  public static ChangeBlockData deserialize(Map<String, Object> map) {
    ChangeBlockData data = new ChangeBlockData();
    data.type = (String) map.get("type");
    data.data = (int) map.get("data");
    data.location = (Location) map.get("location");

    if (Main.getInstance().getProoerties().isDebug()) {
      data.location = data.location.add(0, 2, 0);
    }

    return data;
  }

}
