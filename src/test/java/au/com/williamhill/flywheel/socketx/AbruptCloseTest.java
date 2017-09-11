package au.com.williamhill.flywheel.socketx;

import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.socketx.jetty.*;
import au.com.williamhill.flywheel.socketx.netty.*;
import au.com.williamhill.flywheel.socketx.undertow.*;
import au.com.williamhill.flywheel.util.*;

public final class AbruptCloseTest extends BaseClientServerTest {
  @Test
  public void testJtJtClientClose() throws Exception {
    testClientClose(JettyServer.factory(), JettyClient.factory());
  }
  
  @Test
  public void testUtUtClientClose() throws Exception {
    testClientClose(UndertowServer.factory(), UndertowClient.factory());
  }
  
  @Test
  public void testNtUtClientClose() throws Exception {
    testClientClose(NettyServer.factory(), UndertowClient.factory());
  }

  @Test
  public void testJtJtServerClose() throws Exception {
    testServerClose(JettyServer.factory(), JettyClient.factory());
  }
  
  @Test
  public void testUtUtServerClose() throws Exception {
    testServerClose(UndertowServer.factory(), UndertowClient.factory());
  }
  
  @Test
  public void testNtUtServerClose() throws Exception {
    testServerClose(NettyServer.factory(), UndertowClient.factory());
  }

  private void testClientClose(XServerFactory<? extends XEndpoint> serverFactory,
                               XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    final XServerConfig serverConfig = getDefaultServerConfig(false)
        .withScanInterval(1);
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, serverConfig, serverListener);
    
    final XClientConfig clientConfig = getDefaultClientConfig()
        .withScanInterval(1);
    createClient(clientFactory, clientConfig);

    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    final XEndpoint endpoint = openClientEndpoint(false, serverConfig.port, clientListener);
    SocketTestSupport.await().until(() -> {
      Mockito.verify(serverListener).onConnect(Mocks.anyNotNull());
      Mockito.verify(clientListener).onConnect(Mocks.anyNotNull());
    });
    
    endpoint.terminate();
    SocketTestSupport.await().until(() -> {
      Mockito.verify(serverListener).onClose(Mocks.anyNotNull());
      Mockito.verify(clientListener).onClose(Mocks.anyNotNull());
    });
  }

  private void testServerClose(XServerFactory<? extends XEndpoint> serverFactory,
                               XClientFactory<? extends XEndpoint> clientFactory) throws Exception {
    final XServerConfig serverConfig = getDefaultServerConfig(false);
    serverConfig.scanIntervalMillis = 1;
    final XEndpointListener<XEndpoint> serverListener = createMockListener();
    createServer(serverFactory, serverConfig, serverListener);
    
    final XClientConfig clientConfig = getDefaultClientConfig();
    clientConfig.scanIntervalMillis = 1;
    createClient(clientFactory, clientConfig);

    final XEndpointListener<XEndpoint> clientListener = createMockListener();
    openClientEndpoint(false, serverConfig.port, clientListener);
    
    SocketTestSupport.await().untilTrue(this::hasServerEndpoint);
    
    final XEndpoint endpoint = getServerEndpoint();
    endpoint.terminate();
    SocketTestSupport.await().until(() -> {
      Mockito.verify(serverListener).onClose(Mocks.anyNotNull());
      Mockito.verify(clientListener).onClose(Mocks.anyNotNull());
    });
  }
}
