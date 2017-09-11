package au.com.williamhill.flywheel.socketx;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;
import org.mockito.*;
import org.slf4j.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.socketx.jetty.*;
import au.com.williamhill.flywheel.socketx.netty.*;
import au.com.williamhill.flywheel.socketx.undertow.*;
import au.com.williamhill.flywheel.util.*;

@RunWith(Parameterized.class)
public final class IdleTimeoutTest extends BaseClientServerTest {
  private static final Logger LOG = LoggerFactory.getLogger(IdleTimeoutTest.class);
  private static final int MAX_PORT_USE_COUNT = 10_000;
  private static final int REPEAT = 1;
  
  @Parameterized.Parameters
  public static List<Object[]> data() {
    final Object[][] params = new Object[REPEAT][1];
    for (int i = 0; i < REPEAT; i++) params[i][0] = i;
    return Arrays.asList(params);
  }
  
  @Test
  public void testJtJtServerTimeout() throws Exception {
    // Note: Jetty requires more idle time allowance than others, otherwise the connection
    // times out before it is upgraded to a WebSocket.
    testServerTimeout(JettyServer.factory(), JettyClient.factory(), 500);
  }
  
  @Test
  public void testUtUtServerTimeout() throws Exception {
    testServerTimeout(UndertowServer.factory(), UndertowClient.factory(), 200);
  }
  
  @Test
  public void testNtUtServerTimeout() throws Exception {
    testServerTimeout(NettyServer.factory(), UndertowClient.factory(), 200);
  }
  
  @Test
  public void testJtJtClientTimeout() throws Exception {
    // Note: Jetty requires more idle time allowance than others, otherwise the connection
    // times out before it is upgraded to a WebSocket.
    testClientTimeout(JettyServer.factory(), JettyClient.factory(), 500);
  }
  
  @Parameter(0)
  public int runNo;
  
  @Test
  public void testUtUtClientTimeout() throws Exception {
    testClientTimeout(UndertowServer.factory(), UndertowClient.factory(), 200);
  }
  
  private void testClientTimeout(XServerFactory<? extends XEndpoint> serverFactory,
                                 XClientFactory<? extends XEndpoint> clientFactory,
                                 int idleTimeoutMillis) throws Exception {
    LOG.debug("Started client timeout test {}", runNo);
    final XServerConfig serverConfig = getDefaultServerConfig(false)
        .withScanInterval(1);
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, serverConfig, serverListener);

    final XClientConfig clientConfig = getDefaultClientConfig()
        .withScanInterval(1)
        .withIdleTimeout(idleTimeoutMillis);
    createClient(clientFactory, clientConfig);

    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    openClientEndpoint(false, serverConfig.port, clientListener);
    LOG.debug("Client timeout: Awaiting");
    try {
      SocketTestSupport.await().until(() -> {
        Mockito.verify(serverListener).onConnect(Mocks.anyNotNull());
        Mockito.verify(clientListener).onConnect(Mocks.anyNotNull());
      });
      
      SocketTestSupport.await().until(() -> {
        Mockito.verify(serverListener).onClose(Mocks.anyNotNull());
        Mockito.verify(clientListener).onClose(Mocks.anyNotNull());
      });
    } finally {
      LOG.debug("Ended client timeout test {}", runNo);
    }
  }

  private void testServerTimeout(XServerFactory<? extends XEndpoint> serverFactory,
                                 XClientFactory<? extends XEndpoint> clientFactory,
                                 int idleTimeoutMillis) throws Exception {
    LOG.debug("Started server timeout test");
    final XServerConfig serverConfig = getDefaultServerConfig(false)
        .withScanInterval(1)
        .withIdleTimeout(idleTimeoutMillis);
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, serverConfig, serverListener);

    final XClientConfig clientConfig = getDefaultClientConfig()
        .withScanInterval(1);
    createClient(clientFactory, clientConfig);

    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    openClientEndpoint(false, serverConfig.port, clientListener);
    LOG.debug("Server timeout: Awaiting");
    try {
      SocketTestSupport.await().until(() -> {
        Mockito.verify(serverListener).onClose(Mocks.anyNotNull());
        Mockito.verify(clientListener).onClose(Mocks.anyNotNull());
      });
    } finally {
      LOG.debug("Ended server timeout test");
    }
    
    SocketTestSupport.drainPort(serverConfig.port, MAX_PORT_USE_COUNT);
  }
}
