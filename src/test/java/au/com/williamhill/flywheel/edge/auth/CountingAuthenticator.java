package au.com.williamhill.flywheel.edge.auth;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.util.*;

public class CountingAuthenticator implements Authenticator {
  private final NestedAuthenticator delegate;
  
  private final Map<EdgeNexus, Map<String, AtomicInteger>> counters = new ConcurrentHashMap<>();

  public CountingAuthenticator(NestedAuthenticator delegate) {
    this.delegate = delegate;
  }
  
  @Override
  public void attach(AuthConnector connector) throws Exception {
    delegate.attach(connector);
  }
  
  @Override
  public void close() throws Exception {
    delegate.close();
  }

  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
    final Map<String, AtomicInteger> nexusCounters = Maps.putAtomic(counters, counters, nexus, ConcurrentHashMap::new);
    final AtomicInteger counter = Maps.putAtomic(nexusCounters, nexusCounters, topic, AtomicInteger::new);
    counter.incrementAndGet();
    delegate.verify(nexus, topic, outcome);
  }
  
  public Map<EdgeNexus, Map<String, AtomicInteger>> invocations() {
    return counters;
  }
}
