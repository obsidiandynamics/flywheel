package au.com.williamhill.flywheel.socketx.util;

import java.util.concurrent.atomic.*;

import org.junit.Test;

import junit.framework.*;

public final class AsserterTest {
  @Test
  public void testPass() throws InterruptedException {
    Asserter.wait(20).withIntervalMillis(1).until(() -> {});
  }
  
  @Test
  public void testFail() throws InterruptedException {
    final String message = "Boom";
    try {
      Asserter.wait(20).withIntervalMillis(1).until(() -> { throw new AssertionError(message); });
      TestCase.fail("AssertionError not thrown");
    } catch (AssertionError e) {
      TestCase.assertEquals(message, e.getMessage());
    }
  }
  
  @Test
  public void testPartialFail() throws InterruptedException {
    final AtomicInteger calls = new AtomicInteger();
    Asserter.wait(20).withIntervalMillis(100).until(() -> { 
      if (calls.getAndIncrement() <= 1) {
        throw new AssertionError("Boom"); 
      }
    });
  }
}
