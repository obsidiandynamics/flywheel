package au.com.williamhill.flywheel.socketx.jetty;

import java.net.*;
import java.util.*;

import org.eclipse.jetty.client.*;
import org.eclipse.jetty.util.ssl.*;
import org.eclipse.jetty.util.thread.*;
import org.eclipse.jetty.websocket.client.*;

import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.socketx.ssl.*;

public final class JettyClient implements XClient<JettyEndpoint> {
  private final XClientConfig config;
  
  private final HttpClient httpClient;
  
  private final WebSocketClient client;
  
  private final XEndpointScanner<JettyEndpoint> scanner;
  
  private JettyClient(XClientConfig config, HttpClient httpClient) throws Exception {
    this.config = config;
    this.httpClient = httpClient;
    client = new WebSocketClient(httpClient);
    client.setMaxIdleTimeout(config.idleTimeoutMillis);
    client.start();
    scanner = new XEndpointScanner<>(config.scanIntervalMillis, 0);
  }

  @Override
  public JettyEndpoint connect(URI uri, XEndpointListener<? super JettyEndpoint> listener) throws Exception {
    final JettyEndpoint endpoint = JettyEndpoint.clientOf(scanner, config, listener);
    client.connect(endpoint, uri).get();
    return endpoint;
  }

  @Override
  public void close() throws Exception {
    scanner.close();
    httpClient.stop();
    client.stop();
  }
  
  @Override
  public Collection<JettyEndpoint> getEndpoints() {
    return scanner.getEndpoints();
  }
  
  @Override
  public XClientConfig getConfig() {
    return config;
  }
  
  private static HttpClient createDefaultHttpClient(SSLContextProvider sslContextProvider) throws Exception {
    final SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setSslContext(sslContextProvider.getSSLContext());
    final HttpClient httpClient = new HttpClient(sslContextFactory);
    
    httpClient.setExecutor(new QueuedThreadPool(10_000, 100));
    httpClient.start();
    return httpClient;
  }
  
  public static XClientFactory<JettyEndpoint> factory() {
    return config -> new JettyClient(config, createDefaultHttpClient(config.sslContextProvider));
  }
}
