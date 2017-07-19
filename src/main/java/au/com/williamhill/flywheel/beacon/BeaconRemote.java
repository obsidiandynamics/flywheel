package au.com.williamhill.flywheel.beacon;

import static com.obsidiandynamics.indigo.util.PropertyUtils.*;

import java.net.*;
import java.util.*;

import org.slf4j.*;

import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.remote.*;

public final class BeaconRemote {
  private static final Logger LOG = LoggerFactory.getLogger(BeaconRemote.class);
  
  private static final Properties PROPS = new Properties(System.getProperties());
  private static final String HOST = getOrSet(PROPS, "flywheel.beacon.host", String::valueOf, "localhost");
  private static final int PORT = getOrSet(PROPS, "flywheel.beacon.port", Integer::valueOf, 8080);
  private static final String PATH = getOrSet(PROPS, "flywheel.beacon.path", String::valueOf, "/broker");

  public BeaconRemote() throws Exception {
    filter("flywheel.beacon", PROPS).entrySet().stream()
    .map(e -> String.format("%-30s: %s", e.getKey(), e.getValue())).forEach(LOG::info);
    
    final RemoteNode remote = RemoteNode.builder().build();
    remote.open(new URI(String.format("ws://%s:%d%s", HOST, PORT, PATH)), new RemoteNexusHandler() {
      @Override
      public void onOpen(RemoteNexus nexus) {
        LOG.info("{}: opened", nexus);
        nexus.bind(new BindFrame().withSubscribe("time"));
      }

      @Override
      public void onClose(RemoteNexus nexus) {
        LOG.info("{}: closed", nexus);
      }

      @Override
      public void onText(RemoteNexus nexus, String topic, String payload) {
        LOG.info("{}: text {} {}", nexus, topic, payload);
      }

      @Override
      public void onBinary(RemoteNexus nexus, String topic, byte[] payload) {
        LOG.info("{}: binary {} {}", nexus, topic, payload);
      }
    });
  }
}
