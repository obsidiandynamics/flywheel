package au.com.williamhill.flywheel.socketx.util;

import java.util.function.*;

/**
 *  Adds timed assertion testing to {@link Await}.
 */
public final class Asserter {
  private final int waitMillis;
  
  private int intervalMillis = Await.DEF_INTERVAL;
  
  private Asserter(int waitMillis) {
    this.waitMillis = waitMillis;
  }
  
  public static Asserter wait(int waitMillis) {
    return new Asserter(waitMillis);
  }
  
  public Asserter withIntervalMillis(int intervalMillis) {
    this.intervalMillis = intervalMillis;
    return this;
  }

  private static BooleanSupplier isAsserted(Runnable assertion) {
    return () -> {
      try {
        assertion.run();
        return true;
      } catch (AssertionError e) {
        return false;
      }
    };
  }
  
  public void until(Runnable assertion) throws InterruptedException {
    if (! Await.bounded(waitMillis, intervalMillis, isAsserted(assertion))) {
      assertion.run();
    }
  }
}
