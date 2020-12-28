package jp.thelow.chestShare.util;

import java.util.HashSet;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import jp.thelow.core.LbnRuntimeException;
import jp.thelow.core.Main;

public abstract class LbnRunnable extends BukkitRunnable {
  private static HashSet<LbnRunnable> aliveRunnableList = new HashSet<>();

  public String getName() {
    return "none";
  }

  private long privateTick = 0;

  private long period = 0;
  private long delay = 0;

  boolean isFirst = true;

  int runCount = 0;

  @Override
  final public void run() {
    if (isFirst) {
      isFirst = false;
      privateTick += delay;
    } else {
      privateTick += period;
    }

    try {
      try {
        run2();
      } catch (Exception e) {
        e.printStackTrace();
      }
    } finally {
      runCount++;
    }

    if (isLater) {
      aliveRunnableList.remove(this);
    }

    if (aliveRunnableList.size() > 50000) {
      new LbnRuntimeException("LbnRunnable's instance exsit over 50000").printStackTrace();
    }
  }

  /**
   * 0からスタート
   *
   * @return
   */
  public int getRunCount() {
    return runCount;
  }

  abstract public void run2();

  @Deprecated
  @Override
  public synchronized BukkitTask runTaskLaterAsynchronously(Plugin plugin,
      long delay) throws IllegalArgumentException, IllegalStateException {
    return super.runTaskLaterAsynchronously(plugin, delay);
  }

  @Deprecated
  @Override
  public synchronized BukkitTask runTaskAsynchronously(Plugin plugin)
      throws IllegalArgumentException, IllegalStateException {
    return super.runTaskAsynchronously(plugin);
  }

  @Deprecated
  @Override
  public synchronized BukkitTask runTaskTimerAsynchronously(Plugin plugin,
      long delay, long period) throws IllegalArgumentException,
      IllegalStateException {
    return super.runTaskTimerAsynchronously(plugin, delay, period);
  }

  boolean isLater = false;

  @Override
  public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay,
      long period) throws IllegalArgumentException, IllegalStateException {
    this.delay = delay;
    this.period = period;
    aliveRunnableList.add(this);
    return super.runTaskTimer(plugin, delay, period);
  }

  public synchronized BukkitTask runTaskTimer(long periodTick) throws IllegalArgumentException, IllegalStateException {
    return runTaskTimer(Main.plugin, 0, periodTick);
  }

  @Override
  public synchronized BukkitTask runTaskLater(Plugin plugin, long delay)
      throws IllegalArgumentException, IllegalStateException {
    this.delay = delay;
    aliveRunnableList.add(this);
    this.isLater = true;
    return super.runTaskLater(plugin, delay);
  }

  public long getAgeTick() {
    return privateTick;
  }

  /**
   * 指定した時間以上経過していたらTRUE
   *
   * @param tick
   * @return
   */
  public boolean isElapsedTick(long tick) {
    return privateTick >= tick;
  }

  public boolean isElapsedSecond(long second) {
    return privateTick >= second * 20;
  }

  @Override
  public synchronized void cancel() throws IllegalStateException {
    super.cancel();
    aliveRunnableList.remove(this);
  }

  protected void runIfServerEnd() {}

  public boolean isRunning() {
    return aliveRunnableList.contains(this);
  }

  public static void allCancel() {
    for (LbnRunnable lbnRunnable : aliveRunnableList) {
      lbnRunnable.runIfServerEnd();
    }
    aliveRunnableList.clear();
  }
}
