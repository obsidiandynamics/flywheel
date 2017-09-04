package au.com.williamhill.flywheel.socketx;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.*;

import org.awaitility.*;
import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

public final class XEndpointScannerTest {
  private XEndpointScanner<XEndpoint> scanner;
  
  @After
  public void after() throws InterruptedException {
    if (scanner != null) scanner.close();
  }

  @Test
  public void testUnexpectedError() {
    scanner = new XEndpointScanner<XEndpoint>(1, 0);
    final XEndpoint endpoint = mock(XEndpoint.class);
    when(endpoint.isOpen()).thenThrow(new RuntimeException("boom"));
    scanner.addEndpoint(endpoint);
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(endpoint, atLeastOnce()).isOpen();
    });
  }

  @Test
  public void testTerminateDefunct() {
    scanner = new XEndpointScanner<XEndpoint>(1, 0);
    final XEndpoint endpoint = mock(XEndpoint.class);
    doReturn(true).when(endpoint).isOpen();
    scanner.addEndpoint(endpoint);
    TestSupport.sleep(10);
    doReturn(false).when(endpoint).isOpen();
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(endpoint, atLeastOnce()).terminate();
    });
  }

  @Test
  public void testPing() {
    scanner = new XEndpointScanner<XEndpoint>(1, 10);
    final XEndpoint endpoint = mock(XEndpoint.class);
    when(endpoint.isOpen()).thenReturn(true);
    when(endpoint.getLastActivityTime()).thenReturn(System.currentTimeMillis());
    scanner.addEndpoint(endpoint);
    Awaitility.dontCatchUncaughtExceptions().await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(endpoint, atLeastOnce()).sendPing();
    });
  }
  
  @Test
  public void testAddGetRemove() {
    scanner = new XEndpointScanner<XEndpoint>(1, 1);
    final XEndpoint endpoint = mock(XEndpoint.class);
    scanner.addEndpoint(endpoint);
    assertEquals(1, scanner.getEndpoints().size());
    assertTrue(scanner.getEndpoints().contains(endpoint));
    scanner.removeEndpoint(endpoint);
    assertEquals(0, scanner.getEndpoints().size());
  }
}
