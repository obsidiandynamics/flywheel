package au.com.williamhill.flywheel.edge.auth.httpproxy;

import org.junit.*;

public final class FutureCallbackAdapterTest {
  @Test
  public void test() {
    final FutureCallbackAdapter<Void> callback = new FutureCallbackAdapter<Void>() {};
    callback.completed(null);
    callback.cancelled();
    callback.failed(null);
  }
}
