package au.com.williamhill.flywheel.socketx;

import java.net.*;

import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.util.*;

public abstract class BaseClientServerTest implements TestSupport {
  protected XServer<? extends XEndpoint> server;

  protected XClient<? extends XEndpoint> client;
  
  @Before
  public final void setup() throws Exception {
    init();
  }

  @After
  public final void teardown() throws Exception {
    dispose();
  }
  
  protected void init() throws Exception {}
  
  protected void dispose() throws Exception {
    if (server != null) server.close();
    if (client != null) client.close();
    
    server = null;
    client = null;
  }

  protected static XServerConfig getDefaultServerConfig() {
    return new XServerConfig() {{
      port = SocketTestSupport.getAvailablePort(8080);
    }};
  }

  protected static XClientConfig getDefaultClientConfig() {
    return new XClientConfig();
  }
  
  @SuppressWarnings("unchecked")
  protected final void createServer(XServerFactory<? extends XEndpoint> serverFactory,
                                    XServerConfig config, XEndpointListener<XEndpoint> serverListener) throws Exception {
    server = serverFactory.create(config, Mocks.logger(XEndpointListener.class, 
                                                             serverListener,
                                                             new LoggingInterceptor<>("s: ")));
  }
  
  protected final void createClient(XClientFactory<? extends XEndpoint> clientFactory, XClientConfig config) throws Exception {
    client = clientFactory.create(config);
  }
  
  @SuppressWarnings("unchecked")
  protected final XEndpoint openClientEndpoint(int port, XEndpointListener<XEndpoint> clientListener) throws URISyntaxException, Exception {
    return client.connect(new URI("ws://localhost:" + port + "/"),
                          Mocks.logger(XEndpointListener.class, 
                                       clientListener,
                                       new LoggingInterceptor<>("c: ")));
  }
  
  protected final boolean hasServerEndpoint() {
    return ! server.getEndpointManager().getEndpoints().isEmpty();
  }
  
  protected final XEndpoint getServerEndpoint() {
    return server.getEndpointManager().getEndpoints().iterator().next();
  }
  
  @SuppressWarnings("unchecked")
  protected static XEndpointListener<XEndpoint> createMockListener() {
    return Mockito.mock(XEndpointListener.class);
  }
}
