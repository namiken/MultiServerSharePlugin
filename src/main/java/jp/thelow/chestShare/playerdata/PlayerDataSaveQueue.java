package jp.thelow.chestShare.playerdata;

import java.util.Collection;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import jp.thelow.chestShare.util.TheLowExecutor;
import jp.thelow.core.Main;

public class PlayerDataSaveQueue {

  public static void init() {

    TheLowExecutor.executeTimer(20 * 15, l -> false, l -> {
      new AutoSaveFunction().runTaskTimer(Main.plugin, 0, 3);
    });
  }

  private static class AutoSaveFunction extends BukkitRunnable {

    private Iterator<? extends Player> iterator;

    public AutoSaveFunction() {
      Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
      iterator = onlinePlayers.iterator();
    }

    @Override
    public void run() {
      if (!iterator.hasNext()) {
        cancel();
        return;
      }

      Player player = iterator.next();
      DatabasePlayerDataSaveLogic.save(player);
    }

  }
}
