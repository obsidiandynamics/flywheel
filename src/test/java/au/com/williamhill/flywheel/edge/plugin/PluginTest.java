package au.com.williamhill.flywheel.edge.plugin;

import org.junit.*;
import org.mockito.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.util.*;

public final class PluginTest {
  private static final int PORT = 8090;
  private EdgeNode edge;
  
  @After
  public void after() throws Exception {
    if (edge != null) edge.close();
    edge = null;
  }
  
  @Test
  public void testLifecycle() throws Exception {
    final Plugin mock = Mockito.mock(Plugin.class);
    final EdgeNodeBuilder builder = EdgeNode.builder()
        .withServerConfig(new XServerConfig().withPort(SocketTestSupport.getAvailablePort(PORT)))
        .withPlugins(mock);
    edge = builder.build();
    edge.close();
    
    Mockito.verify(mock).onBuild(Mockito.eq(builder));
    Mockito.verify(mock).onRun(Mockito.eq(edge));
    Mockito.verify(mock).close();
  }
}
