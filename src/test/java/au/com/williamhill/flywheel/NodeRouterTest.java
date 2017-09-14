package au.com.williamhill.flywheel;

import static com.obsidiandynamics.indigo.util.Mocks.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.*;
import java.util.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.frame.Error;
import au.com.williamhill.flywheel.frame.Wire.*;
import au.com.williamhill.flywheel.remote.*;
import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.util.*;

public final class NodeRouterTest {
  private static final int PREFERRED_PORT = 8080;
  
  private Wire wire;

  private Interchange interchange;
  
  private RemoteNexusHandler handler;
 
  private EdgeNode edge;
  
  private RemoteNode remote;
  
  private int port;
  
  @Before
  public void setup() throws Exception {
    port = SocketTestSupport.getAvailablePort(PREFERRED_PORT);
    
    wire = new Wire(true, LocationHint.UNSPECIFIED);
    interchange = new RoutingInterchange();
    handler = mock(RemoteNexusHandler.class);
    
    edge = EdgeNode.builder()
        .withServerConfig(new XServerConfig() {{ port = NodeRouterTest.this.port; }})
        .withWire(wire)
        .withInterchange(logger(interchange))
        .build();
    
    remote = RemoteNode.builder()
        .withWire(wire)
        .build();
  }
  
  @After
  public void teardown() throws Exception {
    if (remote != null) remote.close();
    if (edge != null) edge.close();
    remote = null;
    edge = null;
  }

  @Test
  public void testInternalPubSub() throws Exception {
    final UUID messageId = UUID.randomUUID();
    final RemoteNexus remoteNexus = remote.open(new URI("ws://localhost:" + port + "/"), logger(handler));

    final String topic = "a/b/c";
    final String payload = "hello internal";
    edge.publish(topic, payload); // no subscriber yet - shouldn't be received
    
    final BindFrame bind = new BindFrame(messageId, 
                                         Long.toHexString(Crypto.machineRandom()),
                                         null,
                                         new String[]{"a/b/c"},
                                         new String[]{},
                                         "some-context");
    final BindResponseFrame bindRes = remoteNexus.bind(bind).get();
    
    assertTrue(bindRes.isSuccess());
    assertEquals(FrameType.BIND, bindRes.getType());
    assertArrayEquals(new Error[0], bindRes.getErrors());

    ordered(handler, inOrder -> { // shouldn't have received any data yet
      inOrder.verify(handler).onOpen(notNull(RemoteNexus.class));
    });
    
    edge.publish(topic, payload); // a single subscriber at this point
    
    SocketTestSupport.await().until(() -> {
      verify(handler).onText(notNull(RemoteNexus.class), eq("a/b/c"), eq(payload));
    });
    
    remoteNexus.close();
    
    SocketTestSupport.await().until(() -> {
      verify(handler).onClose(notNull(RemoteNexus.class));
    });
    
    ordered(handler, inOrder -> {
      inOrder.verify(handler).onOpen(notNull(RemoteNexus.class));
      inOrder.verify(handler).onText(notNull(RemoteNexus.class), eq(topic), eq(payload));
      inOrder.verify(handler).onClose(notNull(RemoteNexus.class));
    });
  }

  @Test
  public void testExternalPubSub() throws Exception {
    final UUID messageId = UUID.randomUUID();
    final RemoteNexus remoteNexus = remote.open(new URI("ws://localhost:" + port + "/"), logger(handler));

    final String topic = "a/b/c";
    final String payload = "hello external";
    remoteNexus.publish(new PublishTextFrame(topic, payload)); // no subscriber yet - shouldn't be received
    
    final BindFrame bind = new BindFrame(messageId, 
                                         Long.toHexString(Crypto.machineRandom()),
                                         null,
                                         new String[]{"a/b/c"},
                                         new String[]{},
                                         "some-context");
    final BindResponseFrame bindRes = remoteNexus.bind(bind).get();
    
    assertTrue(bindRes.isSuccess());
    assertEquals(FrameType.BIND, bindRes.getType());
    assertArrayEquals(new Error[0], bindRes.getErrors());

    ordered(handler, inOrder -> { // shouldn't have received any data yet
      inOrder.verify(handler).onOpen(notNull(RemoteNexus.class));
    });
    
    remoteNexus.publish(new PublishTextFrame(topic, payload)); // itself is a subscriber
    
    SocketTestSupport.await().until(() -> {
      verify(handler).onText(notNull(RemoteNexus.class), eq(topic), eq(payload));
    });
    
    remoteNexus.close();
    
    SocketTestSupport.await().until(() -> {
      verify(handler).onClose(notNull(RemoteNexus.class));
    });
    
    ordered(handler, inOrder -> {
      inOrder.verify(handler).onOpen(notNull(RemoteNexus.class));
      inOrder.verify(handler).onText(notNull(RemoteNexus.class), eq(topic), eq(payload));
      inOrder.verify(handler).onClose(notNull(RemoteNexus.class));
    });
  }
}
