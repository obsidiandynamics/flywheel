package au.com.williamhill.flywheel;

import static com.obsidiandynamics.indigo.util.Mocks.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.frame.Error;
import au.com.williamhill.flywheel.frame.Wire.*;
import au.com.williamhill.flywheel.remote.*;
import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.util.*;

public final class NodeCommsTest {
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
    interchange = mock(Interchange.class);
    handler = mock(RemoteNexusHandler.class);
    
    edge = EdgeNode.builder()
        .withServerConfig(new XServerConfig() {{ port = NodeCommsTest.this.port; }})
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
  public void testText() throws Exception {
    final UUID messageId = UUID.randomUUID();
    when(interchange.onBind(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));
    
    final RemoteNexus remoteNexus = remote.open(new URI("ws://localhost:" + port + "/"), logger(handler));
    final String sessionId = Long.toHexString(Crypto.machineRandom());
    final String[] subscribe = new String[]{"a/b/c"};
    final BindFrame bind = new BindFrame(messageId, 
                                         sessionId,
                                         null,
                                         subscribe, 
                                         new String[]{},
                                         "some-context");
    final BindResponseFrame bindRes = remoteNexus.bind(bind).get();
    
    assertTrue(bindRes.isSuccess());
    assertEquals(FrameType.BIND, bindRes.getType());
    assertArrayEquals(new Error[0], bindRes.getErrors());
    
    final PublishTextFrame pubRemote = new PublishTextFrame("x/y/z", "hello from remote");
    remoteNexus.publish(pubRemote);
    
    final EdgeNexus edgeNexus = edge.getNexuses().get(0);
    final TextFrame textEdge = new TextFrame("l/m/n", "hello from edge");
    edgeNexus.send(textEdge).get();
    
    remoteNexus.close();
    
    SocketTestSupport.await().until(() -> {
      verify(interchange).onClose(anyNotNull());
      verify(handler).onClose(anyNotNull());
    });
    
    final Set<String> expectedTopics = new HashSet<>();
    expectedTopics.addAll(Arrays.asList(subscribe));
    expectedTopics.add(Flywheel.getRxTopicPrefix(sessionId));
    expectedTopics.add(Flywheel.getRxTopicPrefix(sessionId) + "/#");
    ordered(interchange, inOrder -> {
      inOrder.verify(interchange).onOpen(anyNotNull());
      inOrder.verify(interchange).onBind(anyNotNull(), eq(expectedTopics), anyNotNull());
      inOrder.verify(interchange).onPublish(anyNotNull(), eq(pubRemote));
      inOrder.verify(interchange).onClose(anyNotNull());
    });
    
    ordered(handler, inOrder -> {
      inOrder.verify(handler).onOpen(anyNotNull());
      inOrder.verify(handler).onText(anyNotNull(), eq(textEdge.getTopic()), eq(textEdge.getPayload()));
      inOrder.verify(handler).onClose(anyNotNull());
    });
  }

  @Test
  public void testBinary() throws Exception {
    final UUID messageId = UUID.randomUUID();
    when(interchange.onBind(any(), any(), anyNotNull())).thenReturn(CompletableFuture.completedFuture(null));
    
    final RemoteNexus remoteNexus = remote.open(new URI("ws://localhost:" + port + "/"), logger(handler));
    final String sessionId = Long.toHexString(Crypto.machineRandom());
    final String[] subscribe = new String[]{"a/b/c"};
    final BindFrame bind = new BindFrame(messageId, 
                                         sessionId,
                                         null,
                                         subscribe,
                                         new String[]{},
                                         "some-context");
    final BindResponseFrame bindRes = remoteNexus.bind(bind).get();
    
    assertTrue(bindRes.isSuccess());
    assertEquals(FrameType.BIND, bindRes.getType());
    assertArrayEquals(new Error[0], bindRes.getErrors());
    
    final PublishBinaryFrame pubRemote = new PublishBinaryFrame("x/y/z", "hello from remote".getBytes());
    remoteNexus.publish(pubRemote);
    
    final EdgeNexus nexus = edge.getNexuses().get(0);
    final BinaryFrame binaryEdge = new BinaryFrame("l/m/n", "hello from edge".getBytes());
    nexus.send(binaryEdge).get();
    
    remoteNexus.close();
    
    SocketTestSupport.await().until(() -> {
      verify(interchange).onClose(anyNotNull());
      verify(handler).onClose(anyNotNull());
    });

    final Set<String> expectedTopics = new HashSet<>();
    expectedTopics.addAll(Arrays.asList(subscribe));
    expectedTopics.add(Flywheel.getRxTopicPrefix(sessionId));
    expectedTopics.add(Flywheel.getRxTopicPrefix(sessionId) + "/#");
    ordered(interchange, inOrder -> {
      inOrder.verify(interchange).onOpen(anyNotNull());
      inOrder.verify(interchange).onBind(anyNotNull(), eq(expectedTopics), anyNotNull());
      inOrder.verify(interchange).onPublish(anyNotNull(), eq(pubRemote));
      inOrder.verify(interchange).onClose(anyNotNull());
    });
    
    ordered(handler, inOrder -> {
      inOrder.verify(handler).onOpen(anyNotNull());
      inOrder.verify(handler).onBinary(anyNotNull(), eq(binaryEdge.getTopic()), eq(binaryEdge.getPayload()));
      inOrder.verify(handler).onClose(anyNotNull());
    });
  }
}
