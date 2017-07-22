package au.com.williamhill.flywheel.edge.plugin.beacon;

import static com.obsidiandynamics.indigo.util.Mocks.*;

import java.net.*;
import java.util.concurrent.*;

import org.awaitility.*;
import org.junit.*;
import org.junit.Test;
import org.mockito.*;
import org.slf4j.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.remote.*;
import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.util.*;
import junit.framework.*;

public final class BeaconTest {
  private static final int PORT = 8090;
  
  private EdgeNode edge;
  
  private RemoteNode remote;
  
  @After
  public void after() throws Exception {
    if (edge != null) edge.close();
    if (remote != null) remote.close();
    
    edge = null;
    remote = null;
  }
  
  @Test
  public void testOverNexus() throws Exception {
    final Logger logger = Mockito.mock(Logger.class);
    final Beacon beacon = new Beacon();
    beacon
    .withLogger(logger)
    .withInterval(1)
    .withTopic("time")
    .withFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    TestCase.assertNotNull(beacon.toString());
    
    Mockito.when(logger.isInfoEnabled()).thenReturn(true);
    Mockito.when(logger.isDebugEnabled()).thenReturn(true);
    
    edge = EdgeNode.builder()
        .withServerConfig(new XServerConfig().withPort(SocketTestSupport.getAvailablePort(PORT)))
        .withPlugins(beacon)
        .build();
    
    remote = RemoteNode.builder().build();
    
    final RemoteNexusHandler handler = Mockito.mock(RemoteNexusHandler.class);
    final RemoteNexus nexus = remote.open(new URI("ws://localhost:" + edge.getServer().getConfig().port), handler);
    nexus.bind(new BindFrame().withSubscribe("time")).get();
    
    final Runnable assertion = () -> {
      Mockito.verify(handler, Mockito.atLeastOnce()).onText(anyNotNull(), anyNotNull(), anyNotNull());
    };
    
    try {
      Awaitility.await().dontCatchUncaughtExceptions().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertion.run());
    } finally {
      assertion.run();
    }
  }
}
