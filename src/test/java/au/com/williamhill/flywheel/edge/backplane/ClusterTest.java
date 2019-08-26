package au.com.williamhill.flywheel.edge.backplane;

import static junit.framework.TestCase.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.socketx.*;
import com.obsidiandynamics.socketx.util.*;
import com.obsidiandynamics.threads.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.remote.*;
import au.com.williamhill.flywheel.util.*;

public abstract class ClusterTest implements TestSupport {
  private static final int PORT = 8090;
  private static final String TOPIC_PREFIX = "topics/";
  
  private final List<EdgeNode> edges = new ArrayList<>();
  
  private final List<RemoteNode> remotes = new ArrayList<>();
  
  protected abstract Backplane getBackplane(String clusterId, String brokerId) throws Exception;  
  
  @Before
  public final void before() throws Exception {
    init();
  }
  
  protected void init() throws Exception {}
  
  @After
  public final void after() throws Exception {
    cleanup();
  }
  
  protected void cleanup() throws Exception {
    for (RemoteNode remote : remotes) {
      remote.close();
    }
    remotes.clear();
    
    for (EdgeNode edge : edges) {
      edge.close();
    }
    edges.clear();
  }
  
  protected final EdgeNode createEdgeNode(int port, Backplane backplane) throws Exception {
    final EdgeNode edge = EdgeNode.builder()
    .withServerConfig(new XServerConfig()
                      .withPort(SocketUtils.getAvailablePort(port))
                      .withHttpsPort(0))
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
    final KeyedBlockingQueue<String, TestBackplaneMessage> received = 
        new KeyedBlockingQueue<>(LinkedBlockingQueue::new);
    
    RetainingSubscriber(RemoteNode remote, URI uri) throws URISyntaxException, Exception {
      nexus = remote.open(uri, this); 
    }

    @Override
    public void onText(RemoteNexus nexus, String topic, String payload) {
      log("s: received (text) %s, topic: %s\n", payload, topic);
      final TestBackplaneMessage msg = TestBackplaneMessage.fromString(payload);
      received.forKey(getKey(msg.brokerId, topic)).add(msg);
    }
    
    @Override
    public void onBinary(RemoteNexus nexus, String topic, byte[] payload) {
      final String str = new String(payload);
      log("s: received (binary) %s, topic: %s\n", str, topic);
      final TestBackplaneMessage msg = TestBackplaneMessage.fromString(str);
      received.forKey(getKey(msg.brokerId, topic)).add(msg);
    }
    
    private static String getKey(String brokerId, String topic) {
      return brokerId + "_" + topic;
    }
  }
  
  private static URI getBrokerURI(int port) throws URISyntaxException {
    return new URI("ws://localhost:" + port + "/");
  }

  protected final void test(int cycles,
                            boolean binary,
                            int edgeNodes, 
                            int subscribersPerNode, 
                            int topics,
                            int messagesPerTopic, 
                            int expectedPartitions,
                            int expectedMessages) throws Exception {
    for (int i = 0; i < cycles; i++) {
      init();
      try {
        test(binary, edgeNodes, subscribersPerNode, topics, messagesPerTopic, expectedPartitions, expectedMessages);
      } finally {
        cleanup();
      }
    }
  }

  private void test(boolean binary,
                    int edgeNodes, 
                    int subscribersPerNode, 
                    int topics,
                    int messagesPerTopic, 
                    int expectedPartitions,
                    int expectedMessages) throws Exception {
    final String clusterId = "TestCluster";
    final RemoteNode remote = createRemoteNode();
    final List<RetainingSubscriber> subscribers = new ArrayList<>(edgeNodes * subscribersPerNode);
    
    for (int i = 0; i < edgeNodes; i++) {
      final int preferredPort = PORT + i;
      final Backplane backplane = getBackplane(clusterId, String.valueOf(i));
      final EdgeNode edge = createEdgeNode(preferredPort, backplane);
      final int port = edge.getServer().getConfig().port; // the actual port may differ from the preferred
      for (int j = 0; j < subscribersPerNode; j++) {
        final RetainingSubscriber sub = new RetainingSubscriber(remote, getBrokerURI(port));
        subscribers.add(sub);
        final CompletableFuture<BindResponseFrame> f = sub.nexus.bind(new BindFrame().withSubscribe(TOPIC_PREFIX + "#"));
        final BindResponseFrame bindRes = f.get();
        assertTrue(bindRes.isSuccess());
      }
    }
    
    final AtomicBoolean error = new AtomicBoolean(false);
    Parallel.blockingSlice(edges, Runtime.getRuntime().availableProcessors(), edgesSlice -> {
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
        
        for (int m = 0; m < messagesPerTopic; m++) {
          final TestBackplaneMessage message = new TestBackplaneMessage(String.valueOf(port), m);
          for (int t = 0; t < topics; t++) {
            if (binary) {
              nexus.publish(new PublishBinaryFrame(TOPIC_PREFIX + t, message.toString().getBytes()));
            } else {
              nexus.publish(new PublishTextFrame(TOPIC_PREFIX + t, message.toString()));
            }
          }
        }
      }
    }).run();
    log("publishing complete\n");
    
    assertFalse(error.get());

    try {
      SocketUtils.await().untilTrue(() -> subscribers.stream().filter(s -> s.received.totalSize() < expectedMessages).count() == 0);
    } finally {
      for (RetainingSubscriber sub : subscribers) {
        assertEquals(expectedPartitions, sub.received.asMap().size());
        assertEquals(expectedMessages, sub.received.totalSize());
        for (Map.Entry<String, BlockingQueue<TestBackplaneMessage>> entry : sub.received.asMap().entrySet()) {
          final List<TestBackplaneMessage> messages = new ArrayList<>(entry.getValue());
          assertEquals(messagesPerTopic, messages.size());
          for (int m = 0; m < messagesPerTopic; m++) {
            final TestBackplaneMessage message = messages.get(m);
            assertEquals(m, message.messageId);
          }
        }
      }
    }
  }
}
