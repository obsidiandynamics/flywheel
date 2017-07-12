package au.com.williamhill.flywheel.beacon;

import static com.obsidiandynamics.indigo.util.PropertyUtils.*;

import java.util.*;

import org.slf4j.*;

import com.obsidiandynamics.indigo.*;
import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.*;
import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.health.*;
import au.com.williamhill.flywheel.socketx.*;

public final class BeaconEdge extends Thread implements TopicListener {
  private static final Logger LOG = LoggerFactory.getLogger(BeaconEdge.class);
  
  private static final Properties PROPS = new Properties(System.getProperties());
  private static final int PORT = getOrSet(PROPS, "flywheel.beacon.port", Integer::valueOf, 8080);
  private static final String PATH = getOrSet(PROPS, "flywheel.beacon.path", String::valueOf, "/beacon");
  private static final int INTERVAL_MILLIS = getOrSet(PROPS, "flywheel.beacon.interval", Integer::valueOf, 1_000);
  
  private final EdgeNode edge;
  
  public BeaconEdge() throws Exception {
    super("BeaconServer");
    filter("flywheel.beacon", PROPS).entrySet().stream()
    .map(e -> String.format("%-30s: %s", e.getKey(), e.getValue())).forEach(LOG::info);
    
    LOG.info("Flywheel version: {}", FlywheelVersion.get());
    LOG.info("Indigo version:   {}", IndigoVersion.get());
    
    edge = EdgeNode.builder()
        .withServerConfig(new XServerConfig()
                          .withPath(PATH)
                          .withPort(PORT)
                          .withServlets(new XMappedServlet("/health/*", HealthServlet.class)))
        .build();
    edge.addTopicListener(this);
    start();
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
    LOG.info("{}: publish {}", nexus, pub);
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
    LOG.info("{}: publish {}", nexus, pub);
  }
  
  @Override
  public void run() {
    for (;;) {
      final String message = new Date().toString();
      for (EdgeNexus nexus : edge.getNexuses()) {
        final String sessionId = nexus.getSession().hasSessionId() ? nexus.getSession().getSessionId() : "anon";
        final String topic = Flywheel.getRxTopicPrefix(sessionId);
        nexus.send(new TextFrame(topic, message));
      }
      try {
        Thread.sleep(INTERVAL_MILLIS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
}
