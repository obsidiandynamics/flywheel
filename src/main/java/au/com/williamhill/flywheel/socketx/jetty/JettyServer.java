package au.com.williamhill.flywheel.socketx.jetty;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.util.thread.*;

import au.com.williamhill.flywheel.socketx.*;

public final class JettyServer implements XServer<JettyEndpoint> {
  private final JettyEndpointManager manager;
  private final Server server;
  private final XEndpointScanner<JettyEndpoint> scanner;
  
  private JettyServer(XServerConfig config,
                      XEndpointListener<? super JettyEndpoint> listener) throws Exception {
    server = new Server(new QueuedThreadPool(100));
    final ServerConnector connector = new ServerConnector(server);
    connector.setPort(config.port);
    server.setConnectors(new Connector[]{connector});
    
    final HandlerCollection handlers = new ContextHandlerCollection();
    server.setHandler(handlers);
    
    scanner = new XEndpointScanner<>(config.scanIntervalMillis, config.pingIntervalMillis);
    manager = new JettyEndpointManager(scanner, config.idleTimeoutMillis, 
                                       config.endpointConfig, listener);
    final ContextHandler wsContext = new ContextHandler(config.contextPath);
    wsContext.setHandler(manager);
    handlers.addHandler(wsContext);
    
    final ServletContextHandler svContext = new ServletContextHandler(null, "/");
    for (XMappedServlet servlet : config.servlets) {
      final ServletHolder holder = new ServletHolder(servlet.getServletName(), servlet.getServletClass());
      svContext.getServletHandler().addServletWithMapping(holder, servlet.getServletMapping());
    }
    handlers.addHandler(svContext);
    server.start();
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
  
  public static XServerFactory<JettyEndpoint> factory() {
    return JettyServer::new;
  }
}