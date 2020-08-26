package jp.thelow.chestShare;

import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

import jp.thelow.chestShare.chest.ChestShareManager;
import jp.thelow.chestShare.domain.ChestData;
import jp.thelow.chestShare.domain.DoubleChestData;

public class Main extends JavaPlugin implements Listener {

  private static Main instance;

  private Properties prooerties;

  @Override
  public void onEnable() {
    ConfigurationSerialization.registerClass(ChestData.class);
    ConfigurationSerialization.registerClass(DoubleChestData.class);

    Main.instance = this;
    this.getConfig().options().copyDefaults(true);
    saveDefaultConfig();
    prooerties = new Properties(this.getConfig());

    getServer().getPluginManager().registerEvents(this, this);
    ChestShareManager.start();

  }

  @Override
  public void onDisable() {
    ChestShareManager.close();
  }

  @EventHandler
  public void InventoryCloseEvent(InventoryCloseEvent e) {
    InventoryView view = e.getView();
    Inventory topInventory = view.getTopInventory();
    InventoryHolder holder = topInventory.getHolder();
    if (holder == null) { return; }

    if (holder instanceof DoubleChest) {
      ChestShareManager.onCloseDoubleChest((DoubleChest) holder);
    } else if (holder instanceof Chest) {
      ChestShareManager.onCloseChest((Chest) holder);
    }
  }

  public static Main getInstance() {
    return instance;
  }

  public Properties getProoerties() {
    return prooerties;
  }

}
