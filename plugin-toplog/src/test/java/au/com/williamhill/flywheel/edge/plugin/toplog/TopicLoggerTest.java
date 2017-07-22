package au.com.williamhill.flywheel.edge.plugin.toplog;

import static com.obsidiandynamics.indigo.util.Mocks.*;
import static junit.framework.TestCase.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.awaitility.*;
import org.junit.*;
import org.mockito.*;
import org.slf4j.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.remote.*;
import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.topic.*;
import au.com.williamhill.flywheel.util.*;

public final class TopicLoggerTest {
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
  public void test() throws Exception {
    @SuppressWarnings("resource")
    final TopicLogger logger = new TopicLogger().withExcludeTopics(Topic.of("t"));
    edge = EdgeNode.builder()
        .withServerConfig(new XServerConfig().withPort(SocketTestSupport.getAvailablePort(PORT)))
        .withPlugins(logger)
        .build();
    
    remote = RemoteNode.builder().build();
    
    final RemoteNexusHandler handler = Mockito.mock(RemoteNexusHandler.class);
    final RemoteNexus nexus = remote.open(new URI("ws://localhost:" + edge.getServer().getConfig().port), handler);
    nexus.bind(new BindFrame().withSubscribe("test")).get();
    nexus.publish(new PublishBinaryFrame("test", "test".getBytes()));
    nexus.publish(new PublishTextFrame("test", "test"));
    
    final Runnable assertion = () -> {
      Mockito.verify(handler).onText(anyNotNull(), anyNotNull(), anyNotNull());
      Mockito.verify(handler).onBinary(anyNotNull(), anyNotNull(), anyNotNull());
    };
    
    try {
      Awaitility.await().dontCatchUncaughtExceptions().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertion.run());
    } finally {
      assertion.run();
    }
  }
  
  @Test
  public void testLog() {
    final Logger logger = Mockito.mock(Logger.class);
    
    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    final TopicLogger toplog = new TopicLogger();
    toplog.withLogger(logger);
    assertNotNull(toplog.toString());

    toplog.onOpen(nexus);
    Mockito.when(logger.isInfoEnabled()).thenReturn(true);
    toplog.onOpen(nexus);
    Mockito.verify(logger).info(Mockito.anyString(), Mockito.eq(nexus));
    Mockito.reset(logger);

    toplog.onBind(nexus, new BindFrame(), new BindResponseFrame(new UUID(0, 0)));
    Mockito.when(logger.isDebugEnabled()).thenReturn(true);
    toplog.onBind(nexus, new BindFrame(), new BindResponseFrame(new UUID(0, 0)));
    Mockito.verify(logger).debug(Mockito.anyString(), Mockito.eq(nexus), Mocks.anyNotNull(), Mocks.anyNotNull());
    Mockito.reset(logger);
    
    toplog.onPublish(nexus, new PublishBinaryFrame("topic", "test".getBytes()));
    Mockito.when(logger.isDebugEnabled()).thenReturn(true);
    toplog.onPublish(nexus, new PublishBinaryFrame("topic", "test".getBytes()));
    Mockito.verify(logger).debug(Mockito.anyString(), Mockito.eq(nexus), Mockito.isA(PublishBinaryFrame.class));
    Mockito.reset(logger);
    
    toplog.onPublish(nexus, new PublishTextFrame("topic", "test"));
    Mockito.when(logger.isDebugEnabled()).thenReturn(true);
    toplog.onPublish(nexus, new PublishTextFrame("topic", "test"));
    Mockito.verify(logger).debug(Mockito.anyString(), Mockito.eq(nexus), Mockito.isA(PublishTextFrame.class));
    Mockito.reset(logger);
    
    toplog.withExcludeTopics(Topic.of("topic"));
    assertNotNull(toplog.toString());
    
    Mockito.when(logger.isDebugEnabled()).thenReturn(true);
    toplog.onPublish(nexus, new PublishTextFrame("topic", "test"));
    Mockito.verify(logger).isDebugEnabled();
    Mockito.verifyNoMoreInteractions(logger);
    Mockito.reset(logger);

    Mockito.when(logger.isDebugEnabled()).thenReturn(true);
    toplog.onPublish(nexus, new PublishTextFrame("other", "test"));
    Mockito.verify(logger).isDebugEnabled();
    Mockito.verify(logger).debug(Mockito.anyString(), Mockito.eq(nexus), Mockito.isA(PublishTextFrame.class));
    Mockito.reset(logger);

    toplog.onClose(nexus);
    Mockito.when(logger.isInfoEnabled()).thenReturn(true);
    toplog.onClose(nexus);
    Mockito.verify(logger).info(Mockito.anyString(), Mockito.eq(nexus));
    Mockito.reset(logger);
    
    toplog.close();
  }
}
