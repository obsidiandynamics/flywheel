package au.com.williamhill.flywheel;

import java.util.*;

import org.slf4j.*;

import com.obsidiandynamics.socketx.*;
import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.edge.plugin.*;
import au.com.williamhill.flywheel.topic.*;

@Y
public final class ConfigLauncher implements Launcher  {
  private Logger logger = LoggerFactory.getLogger(ConfigLauncher.class);
  
  @YInject
  private Backplane backplane = new NoOpBackplane();
  
  @YInject
  private XServerConfig serverConfig = new XServerConfig();
  
  @YInject(type=PubAuthChain.class)
  private AuthChain<PubAuthChain> pubAuthChain = new PubAuthChain();
  
  @YInject(type=SubAuthChain.class)
  private AuthChain<SubAuthChain> subAuthChain = new SubAuthChain();
  
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
  
  public ConfigLauncher withPubAuthChain(AuthChain<PubAuthChain> pubAuthChain) {
    this.pubAuthChain = pubAuthChain;
    return this;
  }
  
  public ConfigLauncher withSubAuthChain(AuthChain<SubAuthChain> subAuthChain) {
    this.subAuthChain = subAuthChain;
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
    .append("\n    HTTPS port: ").append(serverConfig.httpsPort)
    .append("\n    path: ").append(serverConfig.path)
    .append("\n    idle timeout: ").append(serverConfig.idleTimeoutMillis).append(" ms")
    .append("\n    ping interval: ").append(serverConfig.pingIntervalMillis).append(" ms")
    .append("\n    scan interval: ").append(serverConfig.scanIntervalMillis).append(" ms")
    .append("\n    high-water mark: ").append(serverConfig.highWaterMark)
    .append("\n    SSL: ").append(serverConfig.sslContextProvider)
    .append("\n    attributes: ").append(serverConfig.attributes);

    sb.append("\n    servlets:");
    for (XMappedServlet mappedServlet : serverConfig.servlets) {
      sb.append("\n      ").append(mappedServlet);
    }
    
    sb.append("\n  Pub auth chain:");
    listChain(pubAuthChain, sb);

    sb.append("\n  Sub auth chain:");
    listChain(subAuthChain, sb);

    sb.append("\n  Plugins:");
    for (Plugin plugin : plugins) {
      sb.append("\n    ").append(plugin);
    }
    
    logger.info(sb.toString());
    
    return EdgeNode.builder()
        .withServerConfig(serverConfig)
        .withBackplane(backplane)
        .withPubAuthChain(pubAuthChain)
        .withSubAuthChain(subAuthChain)
        .withPlugins(plugins);
  }
  
  private static void listChain(AuthChain<?> chain, StringBuilder sb) {
    for (Map.Entry<Topic, Authenticator> entry : chain.getFilters().entrySet()) {
      sb.append("\n    '").append(entry.getKey()).append("' -> ").append(entry.getValue());
    }
  }
  
  @Override
  public void launch(String[] args) throws Exception {
    edge = makeEdge(args).build();
  }

  @Override
  public void close() throws Exception {
    if (edge != null) edge.close();
  }
}