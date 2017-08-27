package au.com.williamhill.flywheel.edge.auth;

import java.util.concurrent.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.edge.*;

public class DelayedAuthenticator<C extends AuthConnector> implements Authenticator<C> {
  private final Authenticator<C> delegate;
  
  private final long delayMillis;
  
  private ExecutorService executor;

  public DelayedAuthenticator(Authenticator<C> delegate, long delayMillis) {
    this.delegate = delegate;
    this.delayMillis = delayMillis;
  }
  
  @Override
  public void attach(C connector) throws Exception {
    if (executor != null) return;
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
    executor.execute(() -> {
      TestSupport.sleep(delayMillis);
      delegate.verify(nexus, topic, outcome);
    });
  }
}
