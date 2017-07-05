package au.com.williamhill.flywheel.backplane;

import static java.util.concurrent.TimeUnit.*;
import static junit.framework.TestCase.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.awaitility.*;
import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.remote.*;
import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.util.*;

public abstract class BackplaneTest {
  private static final int PORT = 8090;
  private static final String TOPIC = "test/fan";
  
  private final List<EdgeNode> edges = new ArrayList<>();
  
  private final List<RemoteNode> remotes = new ArrayList<>();
  
  protected abstract Backplane getBackplane() throws Exception;
  
  @After
  public final void after() throws Exception {
    cleanup();
  }
  
  protected void cleanup() throws Exception {
    for (EdgeNode edge : edges) {
      edge.close();
    }
    edges.clear();
    
    for (RemoteNode remote : remotes) {
      remote.close();
    }
    remotes.clear();
  }
  
  protected final EdgeNode createEdgeNode(int port, Backplane backplane) throws Exception {
    final EdgeNode edge = EdgeNode.builder()
    .withServerConfig(new XServerConfig().withPort(SocketTestSupport.getAvailablePort(port)))
    .withBackplane(backplane)
    .build();
    
    edges.add(edge);
    return edge;
  }
  
  protected final RemoteNode createRemoteNode() throws Exception {
    final RemoteNode remote = RemoteNode.builder().build();
    remotes.add(remote);
    return remote;
  }
  
  private static final class RetainingSubscriber extends RemoteNexusHandlerBase implements TestSupport {
    private final RemoteNexus nexus;
    private final List<TestMessage> received = new CopyOnWriteArrayList<>();
    
    RetainingSubscriber(RemoteNode remote, URI uri) throws URISyntaxException, Exception {
      nexus = remote.open(uri, this); 
    }

    @Override
    public void onText(RemoteNexus nexus, String topic, String payload) {
      log("s: received (text) %s\n", payload);
      received.add(TestMessage.fromString(payload));
    }
    
    @Override
    public void onBinary(RemoteNexus nexus, String topic, byte[] payload) {
      final String str = new String(payload);
      log("s: received (binary) %s\n", str);
      received.add(TestMessage.fromString(str));
    }
  }
  
  private static URI getBrokerURI(int port) throws URISyntaxException {
    return new URI("ws://localhost:" + port + "/");
  }
  
  private static final class TestMessage {
    final int brokerId;
    final int messageId;
    
    TestMessage(int brokerId, int messageId) {
      this.brokerId = brokerId;
      this.messageId = messageId;
    }

    @Override
    public String toString() {
      return brokerId + "-" + messageId;
    }
    
    static TestMessage fromString(String str) {
      final String[] frags = str.split("-");
      return new TestMessage(Integer.parseInt(frags[0]), Integer.parseInt(frags[1]));
    }
  }

  protected final void testCrossCluster(int cycles,
                                        boolean binary,
                                        int edgeNodes, 
                                        int subscribersPerNode, 
                                        int messages, 
                                        int expectedMessages) throws Exception {
    for (int i = 0; i < cycles; i++) {
      try {
        testCrossCluster(binary, edgeNodes, subscribersPerNode, messages, expectedMessages);
      } finally {
        cleanup();
      }
    }
  }

  private void testCrossCluster(boolean binary,
                                int edgeNodes, 
                                int subscribersPerNode, 
                                int messages, 
                                int expectedMessages) throws Exception {
    final Backplane backplane = getBackplane();
    final RemoteNode remote = createRemoteNode();
    final List<RetainingSubscriber> subscribers = new ArrayList<>(edgeNodes * subscribersPerNode);
    
    for (int i = 0; i < edgeNodes; i++) {
      final int preferredPort = PORT + i;
      final EdgeNode edge = createEdgeNode(preferredPort, backplane);
      final int port = edge.getServer().getConfig().port; // the actual port may differ from the preferred
      for (int j = 0; j < subscribersPerNode; j++) {
        final RetainingSubscriber sub = new RetainingSubscriber(remote, getBrokerURI(port));
        subscribers.add(sub);
        final CompletableFuture<BindResponseFrame> f = sub.nexus.bind(new BindFrame().withSubscribe(TOPIC));
        final BindResponseFrame bindRes = f.get();
        assertTrue(bindRes.isSuccess());
      }
    }
    
    final AtomicBoolean error = new AtomicBoolean(false);
    ParallelJob.blockingSlice(edges, Runtime.getRuntime().availableProcessors(), edgesSlice -> {
      for (EdgeNode edge : edgesSlice) {
        final int port = edge.getServer().getConfig().port;
        final RemoteNexus nexus;
        try {
          nexus = remote.open(getBrokerURI(port), new RemoteNexusHandlerBase());
        } catch (Exception e) {
          error.set(true);
          e.printStackTrace();
          return;
        }
        
        for (int i = 0; i < messages; i++) {
          final TestMessage message = new TestMessage(port, i);
          if (binary) {
            nexus.publish(new PublishBinaryFrame(TOPIC, message.toString().getBytes()));
          } else {
            nexus.publish(new PublishTextFrame(TOPIC, message.toString()));
          }
        }
      }
    }).run();
    
    assertFalse(error.get());

    try {
      Awaitility.await().dontCatchUncaughtExceptions().atMost(60, SECONDS)
      .until(() -> subscribers.stream().filter(s -> s.received.size() < expectedMessages).count() == 0);
    } finally {
      for (RetainingSubscriber sub : subscribers) {
        assertEquals(expectedMessages, sub.received.size());
        
        //TODO assert message order
      }
    }
  }
}
