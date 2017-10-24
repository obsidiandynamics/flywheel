package au.com.williamhill.flywheel.edge.plugin.toplog;

import static junit.framework.TestCase.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.*;
import java.util.*;

import org.junit.*;
import org.slf4j.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.remote.*;
import com.obsidiandynamics.socketx.*;
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
  public void testOverNexus() throws Exception {
    @SuppressWarnings("resource")
    final TopicLogger logger = new TopicLogger().withExcludeTopics(Topic.of("t"));
    edge = EdgeNode.builder()
        .withServerConfig(new XServerConfig().withPort(SocketUtils.getAvailablePort(PORT)))
        .withPlugins(logger)
        .build();
    
    remote = RemoteNode.builder().build();
    
    final RemoteNexusHandler handler = mock(RemoteNexusHandler.class);
    final RemoteNexus nexus = remote.open(new URI("ws://localhost:" + edge.getServer().getConfig().port), handler);
    nexus.bind(new BindFrame().withSubscribe("test")).get();
    nexus.publish(new PublishBinaryFrame("test", "test".getBytes()));
    nexus.publish(new PublishTextFrame("test", "test"));
    
    SocketUtils.await().until(() -> {
      verify(handler).onText(notNull(RemoteNexus.class), notNull(String.class), notNull(String.class));
      verify(handler).onBinary(notNull(RemoteNexus.class), notNull(String.class), notNull(byte[].class));
    });
  }
  
  @Test
  public void testLog() {
    final Logger logger = mock(Logger.class);
    
    final EdgeNexus nexus = new EdgeNexus(null, LocalPeer.instance());
    final TopicLogger toplog = new TopicLogger();
    toplog.withLogger(logger);
    assertNotNull(toplog.toString());

    toplog.onOpen(nexus);
    when(logger.isInfoEnabled()).thenReturn(true);
    toplog.onOpen(nexus);
    verify(logger).info(anyString(), eq(nexus));
    reset(logger);

    toplog.onBind(nexus, new BindFrame(), new BindResponseFrame(new UUID(0, 0)));
    when(logger.isDebugEnabled()).thenReturn(true);
    toplog.onBind(nexus, new BindFrame(), new BindResponseFrame(new UUID(0, 0)));
    verify(logger).debug(anyString(), eq(nexus), notNull(Object.class), notNull(Object.class));
    reset(logger);
    
    toplog.onPublish(nexus, new PublishBinaryFrame("topic", "test".getBytes()));
    when(logger.isDebugEnabled()).thenReturn(true);
    toplog.onPublish(nexus, new PublishBinaryFrame("topic", "test".getBytes()));
    verify(logger).debug(anyString(), eq(nexus), isA(PublishBinaryFrame.class));
    reset(logger);
    
    toplog.onPublish(nexus, new PublishTextFrame("topic", "test"));
    when(logger.isDebugEnabled()).thenReturn(true);
    toplog.onPublish(nexus, new PublishTextFrame("topic", "test"));
    verify(logger).debug(anyString(), eq(nexus), isA(PublishTextFrame.class));
    reset(logger);
    
    toplog.withExcludeTopics(Topic.of("topic"));
    assertNotNull(toplog.toString());
    
    when(logger.isDebugEnabled()).thenReturn(true);
    toplog.onPublish(nexus, new PublishTextFrame("topic", "test"));
    verify(logger).isDebugEnabled();
    verifyNoMoreInteractions(logger);
    reset(logger);

    when(logger.isDebugEnabled()).thenReturn(true);
    toplog.onPublish(nexus, new PublishTextFrame("other", "test"));
    verify(logger).isDebugEnabled();
    verify(logger).debug(anyString(), eq(nexus), isA(PublishTextFrame.class));
    reset(logger);

    toplog.onClose(nexus);
    when(logger.isInfoEnabled()).thenReturn(true);
    toplog.onClose(nexus);
    verify(logger).info(anyString(), eq(nexus));
    reset(logger);
    
    toplog.close();
  }
}
