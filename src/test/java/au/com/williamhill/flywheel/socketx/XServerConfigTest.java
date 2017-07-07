package au.com.williamhill.flywheel.socketx;

import static junit.framework.TestCase.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.health.*;

public final class XServerConfigTest {
  @Test
  public void testToString() {
    TestSupport.assertToString(new XServerConfig());
  }

  @Test
  public void testPort() {
    assertEquals(9090, new XServerConfig().withPort(9090).port);
  }

  @Test
  public void testPath() {
    assertEquals("/foo/bar", new XServerConfig().withPath("/foo/bar").path);
  }

  @Test
  public void testIdleTimout() {
    assertEquals(1000, new XServerConfig().withIdleTimeout(1000).idleTimeoutMillis);
  }

  @Test
  public void testScanInterval() {
    assertEquals(2000, new XServerConfig().withScanInterval(2000).scanIntervalMillis);
  }

  @Test
  public void testPingInterval() {
    assertEquals(3000, new XServerConfig().withPingInterval(3000).pingIntervalMillis);
  }

  @Test
  public void testServlets() {
    final XMappedServlet[] servlets = new XMappedServlet[] { new XMappedServlet("/health", "health", HealthServlet.class) };
    assertEquals(servlets, new XServerConfig().withServlets(servlets).servlets);
  }

  @Test
  public void testEndpointConfig() {
    final XEndpointConfig c = new XEndpointConfig();
    assertEquals(c, new XServerConfig().withEndpointConfig(c).endpointConfig);
  }
}
