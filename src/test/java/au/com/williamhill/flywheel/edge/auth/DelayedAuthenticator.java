package au.com.williamhill.flywheel.edge.auth;

import java.util.concurrent.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.edge.*;

public class DelayedAuthenticator implements Authenticator {
  private final NestedAuthenticator delegate;
  
  private final long delayMillis;
  
  private ExecutorService executor;

  public DelayedAuthenticator(NestedAuthenticator delegate, long delayMillis) {
    this.delegate = delegate;
    this.delayMillis = delayMillis;
  }
  
  @Override
  public void attach(AuthConnector connector) throws Exception {
    executor = Executors.newSingleThreadExecutor();
    delegate.attach(connector);
  }
  
  @Override
  public void close() throws Exception {
    delegate.close();
    if (executor != null) executor.shutdown();
  }

  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
    try {
      executor.execute(() -> {
        TestSupport.sleep(delayMillis);
        delegate.verify(nexus, topic, outcome);
      });
    } catch (RejectedExecutionException rxe) {}
  }
}
