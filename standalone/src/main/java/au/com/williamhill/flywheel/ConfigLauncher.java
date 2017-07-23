package au.com.williamhill.flywheel;

import java.util.*;

import org.slf4j.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.socketx.*;

@Y
public final class ConfigLauncher implements Launcher  {
  private Logger logger = LoggerFactory.getLogger(ConfigLauncher.class);
  
  @YInject
  private Backplane backplane = new NoOpBackplane();
  
  @YInject
  private XServerConfig serverConfig = new XServerConfig();
  
  @YInject
  private Plugin[] plugins = new Plugin[0];
  
  private EdgeNode edge;
  
  public ConfigLauncher withBackplane(Backplane backplane) {
    this.backplane = backplane;
    return this;
  }

  public ConfigLauncher withServerConfig(XServerConfig serverConfig) {
    this.serverConfig = serverConfig;
    return this;
  }

  public ConfigLauncher withPlugins(Plugin... plugins) {
    this.plugins = plugins;
    return this;
  }

  public ConfigLauncher withLogger(Logger logger) {
    this.logger = logger;
    return this;
  }
  
  private EdgeNodeBuilder makeEdge(String[] args) throws Exception {
    final StringBuilder sb = new StringBuilder();
    sb.append("\n  Args: ").append(Arrays.toString(args));
    sb.append("\n  Backplane: ").append(backplane);
    
    sb.append("\n  Server config:")
    .append("\n    port: ").append(serverConfig.port)
    .append("\n    path: ").append(serverConfig.path)
    .append("\n    idle timeout: ").append(serverConfig.idleTimeoutMillis).append(" ms")
    .append("\n    ping interval: ").append(serverConfig.pingIntervalMillis).append(" ms")
    .append("\n    scan interval: ").append(serverConfig.scanIntervalMillis).append(" ms");

    sb.append("\n    servlets:");
    for (XMappedServlet mappedServlet : serverConfig.servlets) {
      sb.append("\n      ").append(mappedServlet);
    }
    
    sb.append("\n    endpoint config:")
    .append("\n      high-water mark: ").append(serverConfig.endpointConfig.highWaterMark);

    sb.append("\n  Plugins:");
    for (Plugin plugin : plugins) {
      sb.append("\n    ").append(plugin);
    }
    
    logger.info(sb.toString());
    
    return EdgeNode.builder()
        .withServerConfig(serverConfig)
        .withBackplane(backplane)
        .withPlugins(plugins);
  }
  
  @Override
  public void launch(String[] args) throws Exception {
    edge = makeEdge(args).build();
  }

  @Override
  public void close() throws Exception {
    edge.close();
  }
}