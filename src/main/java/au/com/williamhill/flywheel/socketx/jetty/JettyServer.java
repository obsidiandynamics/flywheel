package au.com.williamhill.flywheel.socketx.jetty;

import java.util.*;

import javax.net.ssl.*;

import org.eclipse.jetty.http.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.util.ssl.*;
import org.eclipse.jetty.util.thread.*;

import au.com.williamhill.flywheel.socketx.*;

public final class JettyServer implements XServer<JettyEndpoint> {
  private final XServerConfig config;
  private final JettyEndpointManager manager;
  private final Server server;
  private final XEndpointScanner<JettyEndpoint> scanner;

  private JettyServer(XServerConfig config,
                      XEndpointListener<? super JettyEndpoint> listener) throws Exception {
    this.config = config;
    server = new Server(new QueuedThreadPool(100));

    final List<Connector> connectors = new ArrayList<>(2);
    final ServerConnector httpConnector = 
        new ServerConnector(server, null, null, null, -1, -1, 
                            new ConnectionFactory[] { getHttpConnectionFactory() });
    connectors.add(httpConnector);
    httpConnector.setPort(config.port);

    if (config.httpsPort != 0) {
      final ServerConnector httpsConnector = new ServerConnector(server,
                                                                 getSSLConnectionFactory(config.sslContextProvider.getSSLContext()),
                                                                 getHttpConnectionFactory());
      connectors.add(httpsConnector);
      httpsConnector.setPort(config.httpsPort);
    }

    server.setConnectors(connectors.toArray(new Connector[connectors.size()]));

    final HandlerCollection handlers = new ContextHandlerCollection();
    server.setHandler(handlers);

    scanner = new XEndpointScanner<>(config.scanIntervalMillis, config.pingIntervalMillis);
    manager = new JettyEndpointManager(scanner, config.idleTimeoutMillis, 
                                       config, listener);
    final ContextHandler wsContext = new ContextHandler(config.path);
    wsContext.setHandler(manager);
    handlers.addHandler(wsContext);

    final ServletContextHandler svContext = new ServletContextHandler(null, "/");
    for (XMappedServlet servlet : config.servlets) {
      final ServletHolder holder = new ServletHolder(servlet.getName(), servlet.getServletClass());
      svContext.getServletHandler().addServletWithMapping(holder, servlet.getPath());
    }
    handlers.addHandler(svContext);
    server.start();
  }
  
  private static HttpConnectionFactory getHttpConnectionFactory() {
    return new HttpConnectionFactory();
  }
  
  private static SslConnectionFactory getSSLConnectionFactory(SSLContext sslContext) {
    final SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setSslContext(sslContext);
    return new SslConnectionFactory(sslContextFactory,
                                    HttpVersion.HTTP_1_1.asString());
  }

  @Override
  public void close() throws Exception {
    scanner.close();
    server.stop();
  }

  @Override
  public XEndpointManager<JettyEndpoint> getEndpointManager() {
    return manager;
  }

  @Override
  public XServerConfig getConfig() {
    return config;
  }

  public static XServerFactory<JettyEndpoint> factory() {
    return JettyServer::new;
  }
}