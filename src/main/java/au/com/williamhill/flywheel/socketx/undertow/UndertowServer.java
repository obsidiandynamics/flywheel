package au.com.williamhill.flywheel.socketx.undertow;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.xnio.*;

import au.com.williamhill.flywheel.socketx.*;
import io.undertow.*;
import io.undertow.server.handlers.*;
import io.undertow.servlet.*;
import io.undertow.servlet.api.*;

public final class UndertowServer implements XServer<UndertowEndpoint> {
  private final Undertow server;
  private final UndertowEndpointManager manager;
  private final XnioWorker worker;
  private final XEndpointScanner<UndertowEndpoint> scanner;
  
  public static class WrapperServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.getWriter().write("Cruizin'");
    }
  }
  
  private UndertowServer(XServerConfig config,
                         XEndpointListener<? super UndertowEndpoint> listener) throws Exception {
    final int ioThreads = Runtime.getRuntime().availableProcessors();
    final int coreWorkerThreads = 100;
    final int maxWorkerThreads = coreWorkerThreads * 100;
    
    worker = Xnio.getInstance().createWorker(OptionMap.builder()
                                             .set(Options.WORKER_IO_THREADS, ioThreads)
                                             .set(Options.THREAD_DAEMON, true)
                                             .set(Options.WORKER_TASK_CORE_THREADS, coreWorkerThreads)
                                             .set(Options.WORKER_TASK_MAX_THREADS, maxWorkerThreads)
                                             .set(Options.TCP_NODELAY, true)
                                             .getMap());
    
    scanner = new XEndpointScanner<>(config.scanIntervalMillis, config.pingIntervalMillis);
    manager = new UndertowEndpointManager(scanner, config.idleTimeoutMillis, config.endpointConfig, listener);
    
    final DeploymentInfo servletBuilder = Servlets.deployment()
        .setClassLoader(UndertowServer.class.getClassLoader())
        .setDeploymentName("servlet").setContextPath("")
        .addServlets(Servlets.servlet(WrapperServlet.class).addMapping("/health/*"));
    final DeploymentManager servletManager = Servlets.defaultContainer().addDeployment(servletBuilder);
    servletManager.deploy();
    
    final PathHandler handler = Handlers.path()
    .addPrefixPath("/", servletManager.start())
    .addPrefixPath(config.contextPath, Handlers.websocket(manager));
    
    server = Undertow.builder()
        .setWorker(worker)
        .addHttpListener(config.port, "0.0.0.0")
        .setHandler(handler)
        .build();
    server.start();
  }
  
  @Override
  public void close() throws Exception {
    scanner.close();
    server.stop();
    worker.shutdown();
    worker.awaitTermination();
  }

  @Override
  public UndertowEndpointManager getEndpointManager() {
    return manager;
  }
  
  public static final class Factory implements XServerFactory<UndertowEndpoint> {
    @Override public XServer<UndertowEndpoint> create(XServerConfig config,
                                                      XEndpointListener<? super UndertowEndpoint> listener) throws Exception {
      return new UndertowServer(config, listener);
    }
  }
  
  public static XServerFactory<UndertowEndpoint> factory() {
    return new Factory();
  }
}