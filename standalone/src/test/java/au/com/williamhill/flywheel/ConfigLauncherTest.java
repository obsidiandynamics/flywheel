package au.com.williamhill.flywheel;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;
import org.mockito.*;
import org.slf4j.*;

import au.com.williamhill.flywheel.edge.auth.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.edge.plugin.*;
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
  public void testCustomConfig() throws Exception {
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
  
  @Test
  public void testDefaultConfig() throws Exception {
    final Profile profile = Profile.fromFile(new File("conf/default/profile.yaml"));
    assertEquals(1, profile.launchers.length);
    assertEquals(ConfigLauncher.class, profile.launchers[0].getClass());
    launcher = (ConfigLauncher) profile.launchers[0];
  }
}
