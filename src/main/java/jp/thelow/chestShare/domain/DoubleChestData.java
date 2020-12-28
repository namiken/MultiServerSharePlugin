package jp.thelow.chestShare.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import jp.thelow.chestShare.Main;

public class DoubleChestData implements ConfigurationSerializable, ShareData {

  private List<ItemStack> contents;

  private Location rightLocation;
  private Location leftLocation;

  public void setContents(ItemStack[] contents) {
    this.contents = Arrays.asList(contents);
  }

  public void setRightLocation(Location rightLocation) {
    this.rightLocation = rightLocation;
  }

  public void setLeftLocation(Location leftLocation) {
    this.leftLocation = leftLocation;
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("contents", contents);
    dataMap.put("rightLocation", rightLocation);
    dataMap.put("leftLocation", leftLocation);
    return dataMap;
  }

  @Override
  public void applyServer() {
    Block block1 = rightLocation.getBlock();
    if (block1.getType() != Material.CHEST) {
      block1.setType(Material.CHEST);
    }
    Block block2 = leftLocation.getBlock();
    if (block2.getType() != Material.CHEST) {
      block2.setType(Material.CHEST);
    }

    Chest state = (Chest) block2.getState();
    Inventory inventory = state.getInventory();
    ItemStack[] array = contents.toArray(new ItemStack[contents.size()]);

    for (int i = 0; i < array.length; i++) {
      ItemStack itemStack = array[i];
      inventory.setItem(i, itemStack);
    }
  }

  @SuppressWarnings("unchecked")
  public static DoubleChestData deserialize(Map<String, Object> map) {

    //オブジェクトを作る
    DoubleChestData data = new DoubleChestData();
    data.contents = (List<ItemStack>) map.get("contents");
    data.rightLocation = (Location) map.get("rightLocation");
    data.leftLocation = (Location) map.get("leftLocation");

    if (Main.getInstance().getProoerties().isDebug()) {
      data.rightLocation = data.rightLocation.add(0, 2, 0);
      data.leftLocation = data.leftLocation.add(0, 2, 0);
    }

    return data;
  }
}
