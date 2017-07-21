package au.com.williamhill.flywheel;

import java.util.*;

import org.slf4j.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.socketx.*;

@Y
public final class ConfigLauncher implements Launcher, TopicListener  {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigLauncher.class);
  
  @YInject
  public Backplane backplane = new NoOpBackplane();
  
  @YInject
  public XServerConfig serverConfig = new XServerConfig();
  
  @YInject
  public Plugin[] plugins = new Plugin[0];
  
  @YInject
  private String[] logExcludeTopics = new String[0];
  
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
    
    sb.append("\n  Log-excluded topic prefixes:");
    for (String logExcludeTopic : logExcludeTopics) {
      sb.append("\n    ").append(logExcludeTopic);
    }
    
    LOG.info(sb.toString());
    
    final EdgeNode edge = EdgeNode.builder()
        .withServerConfig(serverConfig)
        .withBackplane(backplane)
        .withPlugins(plugins)
        .build();
    edge.addTopicListener(this);
  }
  
  @Override
  public void onOpen(EdgeNexus nexus) {
    LOG.info("{}: opened", nexus);
  }

  @Override
  public void onClose(EdgeNexus nexus) {
    LOG.info("{}: closed", nexus);
  }

  @Override
  public void onBind(EdgeNexus nexus, BindFrame bind, BindResponseFrame bindRes) {
    LOG.info("{}: bind {} -> {}", nexus, bind, bindRes);
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {
    if (shouldLog(pub.getTopic())) LOG.info("{}: publish {}", nexus, pub);
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
    if (shouldLog(pub.getTopic())) LOG.info("{}: publish {}", nexus, pub);
  }
  
  private boolean shouldLog(String topic) {
    if (logExcludeTopics.length == 0) return true;
    
    for (String logExcludeTopic : logExcludeTopics) {
      if (topic.startsWith(logExcludeTopic)) {
        return false;
      }
    }
    return true;
  }
}