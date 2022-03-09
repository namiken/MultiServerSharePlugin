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

public class ChestData implements ConfigurationSerializable, ShareData {

  private List<ItemStack> contents;

  private Location chestLocation;

  public void setContents(ItemStack[] contents) {
    this.contents = Arrays.asList(contents);
  }

  public void setChestLocation(Location chestLocation) {
    this.chestLocation = chestLocation;
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("contents", contents);
    dataMap.put("chestLocation", chestLocation);
    return dataMap;
  }

  @Override
  public void applyServer() {
    if (chestLocation == null) { return; }
    Block block = chestLocation.getBlock();
    if (block.getType() != Material.CHEST) {
      block.setType(Material.CHEST);
    }

    Chest state = (Chest) block.getState();
    Inventory inventory = state.getInventory();
    ItemStack[] array = contents.toArray(new ItemStack[contents.size()]);

    for (int i = 0; i < array.length; i++) {
      ItemStack itemStack = array[i];
      inventory.setItem(i, itemStack);
    }
  }

  @SuppressWarnings("unchecked")
  public static ChestData deserialize(Map<String, Object> map) {

    //オブジェクトを作る
    ChestData data = new ChestData();
    data.contents = (List<ItemStack>) map.get("contents");
    data.chestLocation = (Location) map.get("chestLocation");

    if (Main.getInstance().getProoerties().isDebug()) {
      data.chestLocation = data.chestLocation.add(0, 2, 0);
    }

    return data;
  }

}
