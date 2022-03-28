package jp.thelow.chestShare.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import jp.thelow.chestShare.playerdata.PlayerLimitManager;

public class ServerTeleportLimitListener implements Listener {

  @EventHandler
  public void onInventoryOpenEvent(InventoryOpenEvent e) {
    if (PlayerLimitManager.isLimited(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onInventoryOpenEvent(InventoryClickEvent e) {
    if (PlayerLimitManager.isLimited(e.getWhoClicked())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onInventoryDragEvent(InventoryDragEvent e) {
    if (PlayerLimitManager.isLimited(e.getWhoClicked())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
    if (PlayerLimitManager.isLimited(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteractEvent(PlayerInteractEvent e) {
    if (PlayerLimitManager.isLimited(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerPickupItemEvent(PlayerPickupItemEvent e) {
    if (PlayerLimitManager.isLimited(e.getPlayer())) {
      e.setCancelled(true);
    }
  }
}
