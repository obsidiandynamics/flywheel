package au.com.williamhill.flywheel;

import org.junit.*;
import org.mockito.*;
import org.slf4j.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.health.*;
import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.util.*;

public final class ConfigLauncherTest {
  private static final int PORT = 8090;
  
  private ConfigLauncher launcher;

  @After
  public void after() throws Exception {
    if (launcher != null) launcher.close();
    launcher = null;
  }

  @Test
  public void test() throws Exception {
    final Logger logger = Mockito.mock(Logger.class);
    Mockito.when(logger.isDebugEnabled()).thenReturn(true);
    Mockito.when(logger.isInfoEnabled()).thenReturn(true);
    launcher = new ConfigLauncher();
    launcher
    .withBackplane(new NoOpBackplane())
    .withServerConfig(new XServerConfig()
                      .withPort(SocketTestSupport.getAvailablePort(PORT))
                      .withServlets(new XMappedServlet("/health", HealthServlet.class)))
    .withPubAuthChain(new PubAuthChain())
    .withSubAuthChain(new SubAuthChain())
    .withPlugins(Mockito.mock(Plugin.class))
    .withLogger(logger);
    launcher.launch(new String[0]);
    Mockito.verify(logger, Mockito.atLeastOnce()).info(Mockito.anyString());

    launcher.close();
    launcher = null;
  }
}
