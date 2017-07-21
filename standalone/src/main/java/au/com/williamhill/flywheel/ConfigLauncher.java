package au.com.williamhill.flywheel;

import java.util.*;

import org.slf4j.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.socketx.*;

@Y
public final class ConfigLauncher implements Launcher  {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigLauncher.class);
  
  @YInject
  private final Backplane backplane = new NoOpBackplane();
  
  @YInject
  private final XServerConfig serverConfig = new XServerConfig();
  
  @YInject
  private final Plugin[] plugins = new Plugin[0];
  
  @Override
  public void launch(String[] args) throws Exception {
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
    
    LOG.info(sb.toString());
    
    EdgeNode.builder()
        .withServerConfig(serverConfig)
        .withBackplane(backplane)
        .withPlugins(plugins)
        .build();
  }
}