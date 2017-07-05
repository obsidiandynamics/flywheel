package au.com.williamhill.flywheel.socketx;

import static java.util.concurrent.TimeUnit.*;
import static org.awaitility.Awaitility.*;

import java.util.*;

import org.junit.Test;
import org.mockito.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.socketx.jetty.*;
import au.com.williamhill.flywheel.socketx.netty.*;
import au.com.williamhill.flywheel.socketx.undertow.*;
import au.com.williamhill.flywheel.util.*;
import junit.framework.*;

public final class ConnectDisconnectTest extends BaseClientServerTest {
  private static final int CYCLES = 2;
  private static final int CONNECTIONS = 5;
  private static final int PROGRESS_INTERVAL = 10;
  private static final int MAX_PORT_USE_COUNT = 10_000;

  @Test
  public void testJtJt() throws Exception {
    test(true, CYCLES, CONNECTIONS, JettyServer.factory(), JettyClient.factory());
    test(false, CYCLES, CONNECTIONS, JettyServer.factory(), JettyClient.factory());
  }

  @Test
  public void testUtUt() throws Exception {
    test(true, CYCLES, CONNECTIONS, UndertowServer.factory(), UndertowClient.factory());
    test(false, CYCLES, CONNECTIONS, UndertowServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testNtUt() throws Exception {
    test(true, CYCLES, CONNECTIONS, NettyServer.factory(), UndertowClient.factory());
    test(false, CYCLES, CONNECTIONS, NettyServer.factory(), UndertowClient.factory());
  }

  private void test(boolean clean, int cycles, int connections,
                    XServerFactory<? extends XEndpoint> serverFactory,
                    XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    for (int cycle = 0; cycle < cycles; cycle++) {
      if (cycle != 0) init();
      test(clean, connections, serverFactory, clientFactory);
      dispose();
      if (PROGRESS_INTERVAL != 0 && cycle % PROGRESS_INTERVAL == PROGRESS_INTERVAL - 1) {
        LOG_STREAM.format("cycle %,d\n", cycle);
      }
    }
  }

  private void test(boolean clean, int connections,
                    XServerFactory<? extends XEndpoint> serverFactory,
                    XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    final XServerConfig serverConfig = getDefaultServerConfig();
    serverConfig.scanIntervalMillis = 1;
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, serverConfig, serverListener);

    final XClientConfig clientConfig = getDefaultClientConfig();
    clientConfig.scanIntervalMillis = 1;
    createClient(clientFactory, clientConfig);
    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    final List<XEndpoint> endpoints = new ArrayList<>(connections);
    
    // connect all endpoints
    for (int i = 0; i < connections; i++) {
      endpoints.add(openClientEndpoint(serverConfig.port, clientListener));
    }

    // assert connections on server
    await().dontCatchUncaughtExceptions().atMost(1, SECONDS).untilAsserted(() -> {
      Mockito.verify(serverListener, Mockito.times(connections)).onConnect(Mocks.anyNotNull());
      Mockito.verify(clientListener, Mockito.times(connections)).onConnect(Mocks.anyNotNull());
      throw new AssertionError();
    });

    // disconnect all endpoints and await closure
    for (XEndpoint endpoint : endpoints) {
      if (clean) {
        endpoint.close();
        endpoint.close(); // second close() should do no harm, and shouldn't call the handler a second time
      } else {
        endpoint.terminate();
        endpoint.terminate(); // second terminate() should do no harm, and shouldn't call the handler a second time
      }
    }
    for (XEndpoint endpoint : endpoints) {
      endpoint.awaitClose(Integer.MAX_VALUE);
    }
    
    // assert disconnections on server
    await().dontCatchUncaughtExceptions().atMost(60, SECONDS).untilAsserted(() -> {
      Mockito.verify(serverListener, Mockito.times(connections)).onClose(Mocks.anyNotNull());
      Mockito.verify(clientListener, Mockito.times(connections)).onClose(Mocks.anyNotNull());
    });
    
    TestCase.assertEquals(0, server.getEndpointManager().getEndpoints().size());
    TestCase.assertEquals(0, client.getEndpoints().size());
    
    SocketTestSupport.drainPort(serverConfig.port, MAX_PORT_USE_COUNT);
  }
}