package jp.thelow.chestShare.util;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import jp.thelow.chestShare.Main;

public class TheLowExecutor {

  /**
   * 指定された作業を指定Tick後に実行する。もしdelayTickが0の場合は即時実行される
   *
   * @param delayTick
   * @param runnable
   */
  public static void executeLater(double delayTick, Runnable runnable) {
    if (delayTick == 0) {
      runnable.run();
    } else {
      new BukkitRunnable() {
        @Override
        public void run() {
          runnable.run();
        }
      }.runTaskLater(Main.getInstance(), (long) delayTick);
    }
  }

  /**
   * 指定された作業を指定Tick間隔で終了条件が満たされるまで実行する。
   *
   * @param periodTick 実行間隔(tick)
   * @param isEnd 終了条件
   */
  public static LbnRunnable executeTimer(long periodTick, Predicate<LbnRunnable> isEnd,
      Consumer<LbnRunnable> runnable) {
    return executeTimerWithDelay(0, periodTick, isEnd, runnable);
  }

  /**
   * 指定された作業を指定Tick間隔で終了条件が満たされるまで実行する。
   *
   * @param delayTick 遅延時間(tick)
   * @param periodTick 実行間隔(tick)
   * @param isEnd 終了条件
   */
  public static LbnRunnable executeTimerWithDelay(long delayTick, long periodTick, Predicate<LbnRunnable> isEnd,
      Consumer<LbnRunnable> runnable) {
    LbnRunnable runner = new LbnRunnable() {
      @Override
      public void run2() {
        if (isEnd.test(this)) {
          cancel();
          return;
        }
        runnable.accept(this);
      }
    };
    runner.runTaskTimer(Main.getInstance(), delayTick, periodTick);
    return runner;
  }

  /**
   * 指定された作業を指定Tick間隔で終了条件が満たされるまで実行する。
   *
   * @param periodTick 実行間隔(tick)
   * @param isEnd 終了条件(0開始)
   */
  public static void executeTimer2(long periodTick, IntPredicate isEnd, IntConsumer runnable) {
    executeTimerWithDelay2(0, periodTick, isEnd, runnable);
  }

  /**
   * 指定された作業を指定Tick間隔で終了条件が満たされるまで実行する。
   *
   * @param delayTick 遅延時間(tick)
   * @param periodTick 実行間隔(tick)
   * @param isEnd 終了条件(0開始)
   */
  public static void executeTimerWithDelay2(long delayTick, long periodTick, IntPredicate isEnd, IntConsumer runnable) {
    if (periodTick == 0) {
      LbnRunnable runner = new LbnRunnable() {
        @Override
        public void run2() {
          runnable.accept(getRunCount());
        }
      };
      // 即時処理
      while (!isEnd.test(runner.getRunCount())) {
        runner.run();
      }
    } else {
      LbnRunnable runner = new LbnRunnable() {
        @Override
        public void run2() {
          if (isEnd.test(getRunCount())) {
            cancel();
            return;
          }
          runnable.accept(getRunCount());
        }
      };
      runner.runTaskTimer(Main.getInstance(), delayTick, periodTick);
    }
  }

  /**
   * 指定された作業を指定Tick間隔で指定された時間実行する。
   *
   * @param periodTick 実行間隔(tick)
   * @param endingTick 開始してから終了までの時間(tick) この時間は含めません
   */
  public static void executeTimer(long periodTick, long endingTick, Consumer<LbnRunnable> runnable) {
    executeTimer(periodTick, r -> r.getAgeTick() > endingTick, runnable);
  }

  /**
   * メソッドを実行し戻り値を取得する。もしExceptionが発生した場合はdefaultValueを返す
   *
   * @param supplier
   * @param defaultValue
   * @return
   */
  public static <T> T getObjectIgnoreException(Supplier<T> supplier, T defaultValue) {
    try {
      return supplier.get();
    } catch (Exception e) {
      return defaultValue;
    }
  }

  /**
   * メソッドを実行する。もしExceptionが発生した場合はexceptionConsumerを実行する
   *
   * @param runnable
   * @param exceptionConsumer
   */
  public static void executeIgnoreException(Runnable runnable, Consumer<Exception> exceptionConsumer) {
    try {
      runnable.run();
    } catch (Exception e) {
      exceptionConsumer.accept(e);
    }
  }

  /**
   * メソッドを実行する。もしExceptionが発生した場合はexceptionConsumerを実行する
   *
   * @param runnable
   * @param exceptionConsumer
   */
  public static <T> T executeIgnoreException2(Supplier<T> supplier, Function<Exception, T> exceptionConsumer) {
    try {
      return supplier.get();
    } catch (Exception e) {
      return exceptionConsumer.apply(e);
    }
  }

  /**
   * メソッドを実行し戻り値を取得する。もしExceptionが発生した場合はdefaultValueを返す
   *
   * @param supplier
   * @param defaultValue
   * @return
   */
  public static double getDoubleIgnoreException(DoubleSupplier supplier, double defaultValue) {
    try {
      return supplier.getAsDouble();
    } catch (Exception e) {
      return defaultValue;
    }
  }

  /**
   * メソッドを実行し戻り値を取得する。もしExceptionが発生した場合はdefaultValueを返す
   *
   * @param supplier
   * @param defaultValue
   * @return
   */
  public static int getIntIgnoreException(IntSupplier supplier, int defaultValue) {
    try {
      return supplier.getAsInt();
    } catch (Exception e) {
      return defaultValue;
    }
  }

  /**
   * メソッドを実行し戻り値を取得する。もしExceptionが発生した場合はdefaultValueを返す
   *
   * @param supplier
   * @param defaultValue
   * @return
   */
  public static boolean getBooleanIgnoreException(BooleanSupplier supplier, boolean defaultValue) {
    try {
      return supplier.getAsBoolean();
    } catch (Exception e) {
      return defaultValue;
    }
  }

  /**
   * 条件に満たす場合のみオブジェクトを取得する。満たさない場合はnullをかえす。
   *
   * @param bool 条件
   * @param object 取得するオブジェクトを提供するインスタンス
   * @return オブジェクト or null
   */
  public static <T> T getObjectIfTrue(boolean bool, Supplier<T> object) {
    if (bool) { return object.get(); }
    return null;
  }

  /**
   * 条件に満たす場合のみオブジェクトを取得する。満たさない場合は-1をかえす。
   *
   * @param bool 条件
   * @param object 取得するオブジェクトを提供するインスタンス
   * @return オブジェクト or null
   */
  public static int getIntIfTrue(boolean bool, IntSupplier object) {
    if (bool) { return object.getAsInt(); }
    return -1;
  }

  /**
   * 非同期でタスクを実行する。
   *
   * @param asyncFunc 非同期タスク
   * @param syncFunc 同期タスク
   */
  public static <T> void execAsync(Supplier<T> asyncFunc, Consumer<T> syncFunc) {
    new BukkitRunnable() {
      @Override
      public void run() {
        T result = asyncFunc.get();
        Bukkit.getScheduler().callSyncMethod(Main.getInstance(), () -> {
          try {
            syncFunc.accept(result);
          } catch (Throwable e) {
            e.printStackTrace();
            throw e;
          }
          return null;
        });
      }

    }.runTaskAsynchronously(Main.getInstance());
  }

}
