package au.com.williamhill.flywheel.edge.auth;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;

import org.awaitility.*;
import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.NestedAuthenticator.*;
import au.com.williamhill.flywheel.frame.*;

public final class CachedAuthenticatorTest {
  private static class MockAuthenticator implements NestedAuthenticator {
    private volatile long allowMillis;
    
    MockAuthenticator(long allowMillis) {
      this.allowMillis = allowMillis;
    }
    
    void set(long allowMillis) {
      this.allowMillis = allowMillis;
    }

    @Override
    public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
      if (allowMillis != -1) outcome.allow(allowMillis);
      else outcome.forbidden(topic);
    }
    
    @Override public void attach(AuthConnector connector) {}
    
    @Override public void close() {}
  }
  
  private CachedAuthenticator c;
  
  @After
  public void after() throws Exception {
    if (c != null) c.close();
  }

  @Test
  public void testAllowFinite() throws Exception {
    final MockAuthenticator mock = new MockAuthenticator(30000L);
    final NestedAuthenticator spied = spy(mock);
    c = new CachedAuthenticator(new CachedAuthenticatorConfig().withRunIntervalMillis(1000), spied);
    final AuthenticationOutcome outcome = mock(AuthenticationOutcome.class);
    final EdgeNexus nexus = createNexus();
    final AuthConnector connector = mock(AuthConnector.class);
    when(connector.getActiveTopics(eq(nexus))).thenReturn(Collections.singleton("topic"));
    c.attach(connector);
    c.verify(nexus, "topic", outcome);
    verify(outcome, times(1)).allow(eq(30000L));
    verify(spied, times(1)).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
    
    TestSupport.sleep(1);
    reset(outcome, spied);
    c.verify(nexus, "topic", outcome);
    verify(outcome, times(1)).allow(AdditionalMatchers.leq(30000L));
    verifyNoMoreInteractions(spied);
  }

  @Test
  public void testAllowIndefinite() throws Exception {
    final MockAuthenticator mock = new MockAuthenticator(AuthenticationOutcome.INDEFINITE);
    final NestedAuthenticator spied = spy(mock);
    c = new CachedAuthenticator(new CachedAuthenticatorConfig().withRunIntervalMillis(1000), spied);
    final AuthenticationOutcome outcome = mock(AuthenticationOutcome.class);
    final EdgeNexus nexus = createNexus();
    final AuthConnector connector = mock(AuthConnector.class);
    when(connector.getActiveTopics(eq(nexus))).thenReturn(Collections.singleton("topic"));
    c.attach(connector);
    c.verify(nexus, "topic", outcome);
    verify(outcome, times(1)).allow(eq(AuthenticationOutcome.INDEFINITE));
    verify(spied, times(1)).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
    
    TestSupport.sleep(1);
    reset(outcome, spied);
    c.verify(nexus, "topic", outcome);
    verify(outcome, times(1)).allow(eq(AuthenticationOutcome.INDEFINITE));
    verifyNoMoreInteractions(spied);
  }

  @Test
  public void testDeny() throws Exception {
    final MockAuthenticator mock = new MockAuthenticator(-1);
    final NestedAuthenticator spied = spy(mock);
    c = new CachedAuthenticator(new CachedAuthenticatorConfig().withRunIntervalMillis(0), spied);
    final AuthenticationOutcome outcome = mock(AuthenticationOutcome.class);
    final EdgeNexus nexus = createNexus();
    c.attach(mock(AuthConnector.class));
    
    c.verify(nexus, "topic", outcome);
    verify(outcome, times(1)).deny(notNull(TopicAccessError.class));
    verify(spied, times(1)).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
    
    c.verify(nexus, "topic", outcome);
    verify(outcome, times(2)).deny(notNull(TopicAccessError.class));
    verify(spied, times(2)).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
    
    assertNotNull(c.toString());
  }
  
  @Test
  public void testCacheRefreshShortMinIntervalThenPurge() throws Exception {
    final MockAuthenticator mock = new MockAuthenticator(1000L);
    final CountingAuthenticator counting = new CountingAuthenticator(mock);
    final NestedAuthenticator spied = spy(counting);
    c = new CachedAuthenticator(new CachedAuthenticatorConfig()
                                .withRunIntervalMillis(1)
                                .withMinQueryIntervalMillis(1), 
                                spied);
    final AuthenticationOutcome outcome = mock(AuthenticationOutcome.class);
    final EdgeNexus nexus = createNexus();
    final AuthConnector connector = mock(AuthConnector.class);
    when(connector.getActiveTopics(eq(nexus))).thenReturn(Arrays.asList("topic1", "topic2"));
    c.attach(connector);
    c.verify(nexus, "topic1", outcome);
    c.verify(nexus, "topic2", outcome);
    verify(outcome, times(2)).allow(eq(1000L));
    verify(spied).verify(eq(nexus), eq("topic1"), notNull(AuthenticationOutcome.class));
    verify(spied).verify(eq(nexus), eq("topic2"), notNull(AuthenticationOutcome.class));
    
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(spied, atLeast(10)).verify(eq(nexus), eq("topic1"), notNull(AuthenticationOutcome.class));
      verify(spied, atLeast(10)).verify(eq(nexus), eq("topic2"), notNull(AuthenticationOutcome.class));
    });

    // remove all topics from the active set
    when(connector.getActiveTopics(eq(nexus))).thenReturn(Collections.emptySet());
    
    // give the watchdog a chance to run; afterwards there should be no more queries to the delegate
    TestSupport.sleep(100);
    final int countTopic1 = counting.invocations().get(nexus).get("topic1").get();
    final int countTopic2 = counting.invocations().get(nexus).get("topic2").get();
    verify(spied, times(countTopic1)).verify(eq(nexus), eq("topic1"), notNull(AuthenticationOutcome.class));
    verify(spied, times(countTopic2)).verify(eq(nexus), eq("topic2"), notNull(AuthenticationOutcome.class));
  }
  
  @Test
  public void testCacheRefreshShortMinIntervalCappedPending() throws Exception {
    final NestedAuthenticator delegate = new MockAuthenticator(1000L);
    final DelayedAuthenticator delayed = new DelayedAuthenticator(delegate, 100);
    final CountingAuthenticator counting = new CountingAuthenticator(delayed);
    final NestedAuthenticator delegateProxy = spy(counting);
    c = new CachedAuthenticator(new CachedAuthenticatorConfig()
                                .withRunIntervalMillis(1)
                                .withMinQueryIntervalMillis(1)
                                .withMaxPendingQueries(1), 
                                delegateProxy);
    final AuthenticationOutcome outcome = mock(AuthenticationOutcome.class);
    final EdgeNexus nexus = createNexus();
    final AuthConnector connector = mock(AuthConnector.class);
    when(connector.getActiveTopics(eq(nexus))).thenReturn(Arrays.asList("topic1", "topic2"));
    c.attach(connector);
    c.verify(nexus, "topic1", outcome);
    c.verify(nexus, "topic2", outcome);
    verify(delegateProxy, times(1)).verify(eq(nexus), eq("topic1"), notNull(AuthenticationOutcome.class));
    verify(delegateProxy, times(1)).verify(eq(nexus), eq("topic2"), notNull(AuthenticationOutcome.class));
    verify(outcome, times(0)).allow(eq(1000L));

    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(outcome, times(2)).allow(eq(1000L));
    });
    
    TestSupport.sleep(100);
    
    verify(delegateProxy, atMost(4)).verify(eq(nexus), eq("topic1"), notNull(AuthenticationOutcome.class));
    verify(delegateProxy, atMost(4)).verify(eq(nexus), eq("topic2"), notNull(AuthenticationOutcome.class));
  }
  
  @Test
  public void testCacheRefreshShortMinIntervalLongExpiry() throws Exception {
    final MockAuthenticator mock = new MockAuthenticator(30_000);
    final NestedAuthenticator spied = spy(mock);
    c = new CachedAuthenticator(new CachedAuthenticatorConfig()
                                .withRunIntervalMillis(1)
                                .withMinQueryIntervalMillis(1)
                                .withQueryBeforeExpiryMillis(10_000),
                                spied);
    final AuthenticationOutcome outcome = mock(AuthenticationOutcome.class);
    final EdgeNexus nexus = createNexus();
    final AuthConnector connector = mock(AuthConnector.class);
    when(connector.getActiveTopics(eq(nexus))).thenReturn(Collections.singleton("topic"));
    c.attach(connector);
    c.verify(nexus, "topic", outcome);
    verify(outcome, times(1)).allow(eq(30_000L));
    verify(spied, times(1)).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
    
    TestSupport.sleep(100);
    
    verify(spied, times(1)).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
  }
  
  @Test
  public void testCacheRefreshLongMinInterval() throws Exception {
    final MockAuthenticator mock = new MockAuthenticator(1000L);
    final NestedAuthenticator spied = spy(mock);
    c = new CachedAuthenticator(new CachedAuthenticatorConfig()
                                .withRunIntervalMillis(1)
                                .withMinQueryIntervalMillis(1000), 
                                spied);
    final AuthenticationOutcome outcome = mock(AuthenticationOutcome.class);
    final EdgeNexus nexus = createNexus();
    final AuthConnector connector = mock(AuthConnector.class);
    when(connector.getActiveTopics(eq(nexus))).thenReturn(Collections.singleton("topic"));
    c.attach(connector);
    c.verify(nexus, "topic", outcome);
    verify(outcome, times(1)).allow(eq(1000L));
    verify(spied, times(1)).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
    
    TestSupport.sleep(100);
    
    verify(spied, times(1)).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
  }
  
  @Test
  public void testCacheFiniteThenIndefinite() throws Exception {
    final MockAuthenticator mock = new MockAuthenticator(1000);
    final CountingAuthenticator counting = new CountingAuthenticator(mock);
    final NestedAuthenticator spied = spy(counting);
    c = new CachedAuthenticator(new CachedAuthenticatorConfig()
                                .withRunIntervalMillis(1)
                                .withMinQueryIntervalMillis(1)
                                .withQueryBeforeExpiryMillis(10_000),
                                spied);
    final AuthenticationOutcome outcome = mock(AuthenticationOutcome.class);
    final EdgeNexus nexus = createNexus();
    final AuthConnector connector = mock(AuthConnector.class);
    when(connector.getActiveTopics(eq(nexus))).thenReturn(Collections.singleton("topic"));
    c.attach(connector);
    c.verify(nexus, "topic", outcome);
    verify(outcome, times(1)).allow(eq(1000L));
    verify(spied).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
    
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(spied, atLeast(10)).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
    });
    
    // set the mock response to indefinite, which should stop further cache refreshes
    mock.set(AuthenticationOutcome.INDEFINITE);
    TestSupport.sleep(100);
    final int count = counting.invocations().get(nexus).get("topic").get();
    
    verify(spied, times(count)).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
  }
  
  @Test
  public void testCacheFiniteThenDeny() throws Exception {
    final MockAuthenticator mock = new MockAuthenticator(1000);
    final CountingAuthenticator counting = new CountingAuthenticator(mock);
    final NestedAuthenticator spied = spy(counting);
    c = new CachedAuthenticator(new CachedAuthenticatorConfig()
                                .withRunIntervalMillis(1)
                                .withMinQueryIntervalMillis(1)
                                .withQueryBeforeExpiryMillis(10_000),
                                spied);
    final AuthenticationOutcome outcome = mock(AuthenticationOutcome.class);
    final EdgeNexus nexus = createNexus();
    final AuthConnector connector = mock(AuthConnector.class);
    when(connector.getActiveTopics(eq(nexus))).thenReturn(Collections.singleton("topic"));
    c.attach(connector);
    c.verify(nexus, "topic", outcome);
    verify(outcome, times(1)).allow(eq(1000L));
    verify(spied).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
    
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(spied, atLeast(10)).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
    });
    
    // set the mock response to deny, which should cause an expiry at the connector
    mock.set(-1);
    TestSupport.sleep(100);
    verify(connector).expireTopic(eq(nexus), eq("topic"));
    final int count = counting.invocations().get(nexus).get("topic").get();
    verify(spied, times(count)).verify(eq(nexus), eq("topic"), notNull(AuthenticationOutcome.class));
  }

  private static EdgeNexus createNexus() {
    return new EdgeNexus(null, LocalPeer.instance());
  }
}
