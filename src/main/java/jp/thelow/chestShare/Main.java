package jp.thelow.chestShare;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

import jp.thelow.chestShare.chest.ChestShareManager;
import jp.thelow.chestShare.command.ReloadConfigCommand;
import jp.thelow.chestShare.command.TeleportServerCommand;
import jp.thelow.chestShare.domain.ChestData;
import jp.thelow.chestShare.domain.DoubleChestData;

public class Main extends JavaPlugin implements Listener {

  public static final String TP_PLAYER_FOLDER_NAME = "tpPlayer";

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

    createDir();

    getServer().getPluginManager().registerEvents(this, this);
    ChestShareManager.start();

    //コマンド
    Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    getCommand("tpServer").setExecutor(new TeleportServerCommand());
    getCommand("csReload").setExecutor(new ReloadConfigCommand());
  }

  protected void createDir() {
    File file1 = new File(prooerties.getReadFileDir(), TP_PLAYER_FOLDER_NAME);
    file1.mkdirs();
    File file2 = new File(prooerties.getWriteFileDir(), TP_PLAYER_FOLDER_NAME);
    file2.mkdirs();
  }

  @Override
  public void onDisable() {
    ChestShareManager.close();
  }

  //  @EventHandler
  //  public void onJoin(PlayerJoinEvent event) {
  //    TheLowExecutor.executeLater(20 * 3, () -> {
  //      File file = new File(prooerties.getWriteFileDir() + File.separator + TP_PLAYER_FOLDER_NAME,
  //          event.getPlayer().getUniqueId().toString() + ".txt");
  //      try {
  //        file.createNewFile();
  //      } catch (IOException e) {
  //        e.printStackTrace();
  //        //ignore
  //      }
  //    });
  //  }

  @EventHandler
  public void InventoryCloseEvent(InventoryCloseEvent e) {
    InventoryView view = e.getView();
    Inventory topInventory = view.getTopInventory();
    if (topInventory.getType() != InventoryType.CHEST) { return; }

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
