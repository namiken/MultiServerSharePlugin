package jp.thelow.chestShare;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import jp.thelow.chestShare.chest.DataSharedManager;
import jp.thelow.chestShare.command.ReloadConfigCommand;
import jp.thelow.chestShare.command.TeleportServerCommand;
import jp.thelow.chestShare.command.TestPlayerDataCommand;
import jp.thelow.chestShare.domain.ChangeBlockData;
import jp.thelow.chestShare.domain.ChestData;
import jp.thelow.chestShare.domain.DoubleChestData;
import jp.thelow.chestShare.listener.PlayerDataListener;
import jp.thelow.chestShare.listener.ServerTeleportLimitListener;
import jp.thelow.chestShare.playerdata.DatabasePlayerDataSaveLogic;
import jp.thelow.chestShare.playerdata.PlayerDataSaveQueue;
import jp.thelow.chestShare.util.PlayerNameUtil;

public class Main extends JavaPlugin implements Listener {

  public static final String TP_PLAYER_FOLDER_NAME = "tpPlayer";

  public static Set<Player> noClickMap = new HashSet<>();

  private static Main instance;

  private Properties prooerties;

  @Override
  public void onEnable() {
    ConfigurationSerialization.registerClass(ChestData.class);
    ConfigurationSerialization.registerClass(DoubleChestData.class);
    ConfigurationSerialization.registerClass(ChangeBlockData.class);

    Main.instance = this;
    this.getConfig().options().copyDefaults(true);
    saveDefaultConfig();
    prooerties = new Properties(this.getConfig());

    createDir();

    getServer().getPluginManager().registerEvents(this, this);
    getServer().getPluginManager().registerEvents(new ServerTeleportLimitListener(), this);
    getServer().getPluginManager().registerEvents(new PlayerDataListener(), this);
    DataSharedManager.start();

    //コマンド
    Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    getCommand("tpServer").setExecutor(new TeleportServerCommand());
    getCommand("csReload").setExecutor(new ReloadConfigCommand());
    getCommand("test_save").setExecutor(new TestPlayerDataCommand());

    PlayerDataSaveQueue.init();
    PlayerNameUtil.init();
  }

  protected void createDir() {
    File file1 = new File(prooerties.getReadFileDir(), TP_PLAYER_FOLDER_NAME);
    file1.mkdirs();
    File file2 = new File(prooerties.getWriteFileDir(), TP_PLAYER_FOLDER_NAME);
    file2.mkdirs();
  }

  @Override
  public void onDisable() {
    DataSharedManager.close();
    DatabasePlayerDataSaveLogic.saveSyncAllPlayer();
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

  public void changeBlock(Block block) {
    new BukkitRunnable() {
      @Override
      public void run() {
        DataSharedManager.onChangeBlock(block);
      }
    }.runTaskLater(this, 20);

  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    noClickMap.remove(e.getPlayer());
  }

  @EventHandler
  public void onInventoryOpenEvent(InventoryOpenEvent e) {
    if (noClickMap.contains(e.getPlayer())) {
      e.setCancelled(true);
      e.getPlayer().sendMessage(ChatColor.GREEN + "カウントダウン中のため、クリック操作をキャンセルします。");
      getLogger().info("カウントダウン中のため、クリック操作をキャンセルします。:" + e.getPlayer().getName());
    }
  }

  @EventHandler
  public void InventoryCloseEvent(InventoryCloseEvent e) {
    InventoryView view = e.getView();
    Inventory topInventory = view.getTopInventory();
    if (topInventory.getType() != InventoryType.CHEST) { return; }

    InventoryHolder holder = topInventory.getHolder();
    if (holder == null) { return; }

    if (holder instanceof DoubleChest) {
      DataSharedManager.onCloseDoubleChest((DoubleChest) holder);
    } else if (holder instanceof Chest) {
      DataSharedManager.onCloseChest((Chest) holder);
    }
  }

  public static Main getInstance() {
    return instance;
  }

  public Properties getProoerties() {
    return prooerties;
  }
}
