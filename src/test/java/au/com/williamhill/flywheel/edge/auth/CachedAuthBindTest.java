package au.com.williamhill.flywheel.edge.auth;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;

import org.awaitility.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.NestedAuthenticator.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.remote.*;

@RunWith(Parameterized.class)
public final class CachedAuthBindTest extends AbstractAuthTest {
  private static final int REPEAT = 1;
  
  @Parameterized.Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[REPEAT][0]);
  }
  
  private CachedAuthenticator c;

  @Override
  protected void teardown() throws Exception {
    if (c != null) c.close();
  }

  @SuppressWarnings("resource")
  @Test
  public void testAllowLongExpiryThenShortExpiry() throws Exception {
    final MockAuthenticator spied = spy(new MockAuthenticator(30_000));
    c = new CachedAuthenticator(new CachedAuthenticatorConfig()
                                .withRunIntervalMillis(1)
                                .withResidenceTimeMillis(60_000)
                                .withMinQueryIntervalMillis(1), 
                                spied);
    setupEdgeNode(new PubAuthChain(),
                  new SubAuthChain()
                  .set("", c));

    final RemoteNexus remoteNexus = openNexus();
    final String sessionId = generateSessionId();

    final BindFrame bind1 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          null,
                                          new String[]{"topic1", "topic2"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind1Res = remoteNexus.bind(bind1).get();
    assertTrue(bind1Res.isSuccess());
    verify(spied, times(1)).verify(notNull(EdgeNexus.class), eq("topic1"), notNull(AuthenticationOutcome.class));
    verify(spied, times(1)).verify(notNull(EdgeNexus.class), eq("topic2"), notNull(AuthenticationOutcome.class));

    final BindFrame unbind1 = new BindFrame(UUID.randomUUID(), 
                                            sessionId,
                                            null,
                                            new String[]{},
                                            new String[]{"topic1", "topic2"},
                                            null);
    final BindResponseFrame unbind1Res = remoteNexus.bind(unbind1).get();
    assertTrue(unbind1Res.isSuccess());
    verify(spied, times(1)).verify(notNull(EdgeNexus.class), eq("topic1"), notNull(AuthenticationOutcome.class));
    verify(spied, times(1)).verify(notNull(EdgeNexus.class), eq("topic2"), notNull(AuthenticationOutcome.class));
    
    // binding again immediately after an unbind shouldn't result in a query, as the entry is still cached
    final BindFrame bind2 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          null,
                                          new String[]{"topic1", "topic2"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind2Res = remoteNexus.bind(bind2).get();
    assertTrue(bind2Res.isSuccess());
    verify(spied, times(1)).verify(notNull(EdgeNexus.class), eq("topic1"), notNull(AuthenticationOutcome.class));
    verify(spied, times(1)).verify(notNull(EdgeNexus.class), eq("topic2"), notNull(AuthenticationOutcome.class));

    final BindFrame unbind2 = new BindFrame(UUID.randomUUID(), 
                                            sessionId,
                                            null,
                                            new String[]{},
                                            new String[]{"topic1", "topic2"},
                                            null);
    final BindResponseFrame unbind2Res = remoteNexus.bind(unbind2).get();
    assertTrue(unbind2Res.isSuccess());
    verify(spied, times(1)).verify(notNull(EdgeNexus.class), eq("topic1"), notNull(AuthenticationOutcome.class));
    verify(spied, times(1)).verify(notNull(EdgeNexus.class), eq("topic2"), notNull(AuthenticationOutcome.class));
    
    // settings this has no effect on topic1 and topic2, as the entry is cached for a long time;
    // however, topic3 should be queried aggressively
    spied.set(1000L);
    final BindFrame bind3 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          null,
                                          new String[]{"topic1", "topic2", "topic3/+"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind3Res = remoteNexus.bind(bind3).get();
    assertTrue(bind3Res.isSuccess());
    
    verify(spied, times(1)).verify(notNull(EdgeNexus.class), eq("topic1"), notNull(AuthenticationOutcome.class));
    verify(spied, times(1)).verify(notNull(EdgeNexus.class), eq("topic2"), notNull(AuthenticationOutcome.class));
    verify(spied, atLeast(1)).verify(notNull(EdgeNexus.class), eq("topic3/+"), notNull(AuthenticationOutcome.class));
    
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(spied, atLeast(10)).verify(notNull(EdgeNexus.class), eq("topic3/+"), notNull(AuthenticationOutcome.class));
    });
  }

  @SuppressWarnings("resource")
  @Test
  public void testDeny() throws Exception {
    final MockAuthenticator spied = spy(new MockAuthenticator(-1));
    c = new CachedAuthenticator(new CachedAuthenticatorConfig()
                                .withRunIntervalMillis(1)
                                .withResidenceTimeMillis(1000)
                                .withMinQueryIntervalMillis(1), 
                                spied);
    setupEdgeNode(new PubAuthChain(),
                  new SubAuthChain()
                  .set("topic1", c)
                  .set("topic2", c));

    final RemoteNexus remoteNexus = openNexus();
    final String sessionId = generateSessionId();

    final BindFrame bind1 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          null,
                                          new String[]{"topic1", "topic2"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind1Res = remoteNexus.bind(bind1).get();
    assertFalse(bind1Res.isSuccess());
    verify(spied, times(1)).verify(notNull(EdgeNexus.class), eq("topic1"), notNull(AuthenticationOutcome.class));
    verify(spied, times(1)).verify(notNull(EdgeNexus.class), eq("topic2"), notNull(AuthenticationOutcome.class));

    final BindFrame bind2 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          null,
                                          new String[]{"topic1"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind2Res = remoteNexus.bind(bind2).get();
    assertFalse(bind2Res.isSuccess());
    verify(spied, times(2)).verify(notNull(EdgeNexus.class), eq("topic1"), notNull(AuthenticationOutcome.class));
  }
  
  @SuppressWarnings("resource")
  @Test
  public void testAllowThenExpireAndDisconnect() throws Exception {
    final MockAuthenticator spied = spy(new MockAuthenticator(1_000));
    c = new CachedAuthenticator(new CachedAuthenticatorConfig()
                                .withRunIntervalMillis(1)
                                .withResidenceTimeMillis(1000)
                                .withMinQueryIntervalMillis(1), 
                                spied);
    setupEdgeNode(new PubAuthChain(),
                  new SubAuthChain()
                  .set("topic", c));

    final RemoteNexus remoteNexus = openNexus();
    final String sessionId = generateSessionId();

    final BindFrame bind1 = new BindFrame(UUID.randomUUID(), 
                                          sessionId,
                                          null,
                                          new String[]{"topic"},
                                          new String[]{},
                                          null);
    final BindResponseFrame bind1Res = remoteNexus.bind(bind1).get();
    assertTrue(bind1Res.isSuccess());
    verify(spied, atLeast(1)).verify(notNull(EdgeNexus.class), eq("topic"), notNull(AuthenticationOutcome.class));
    
    spied.set(-1);
    
    Awaitility.dontCatchUncaughtExceptions()
    .await().atMost(10, TimeUnit.SECONDS).until(() -> ! remoteNexus.getEndpoint().isOpen());
  }
}
