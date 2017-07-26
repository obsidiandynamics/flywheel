package au.com.williamhill.flywheel.rig;

import static com.obsidiandynamics.indigo.util.PropertyUtils.*;

import java.net.*;

import com.obsidiandynamics.indigo.benchmark.*;
import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.remote.*;
import au.com.williamhill.flywheel.rig.RemoteRig.*;
import au.com.williamhill.flywheel.rig.DoubleRigBenchmark.*;
import au.com.williamhill.flywheel.topic.*;

public final class RemoteRigBenchmark implements TestSupport {
  private static final String URL = get("flywheel.rig.url", String::valueOf, "ws://localhost:8080/broker");
  private static final int SYNC_FRAMES = get("flywheel.rig.syncFrames", Integer::valueOf, 10);
  private static final boolean INITIATE = get("flywheel.rig.initiate", Boolean::valueOf, true);
  private static final double NORMAL_MIN = get("flywheel.rig.normalMin", Double::valueOf, 50_000d);
  
  private static Summary run(Config c) throws Exception {
    final RemoteNode remote = RemoteNode.builder()
        .build();
    final RemoteRig remoteRig = new RemoteRig(remote, new RemoteRigConfig() {{
      topicSpec = c.topicSpec;
      syncFrames = c.syncFrames;
      uri = getUri(c.host, c.port, c.path);
      initiate = c.initiate;
      normalMinNanos = c.normalMinNanos;
      log = c.log;
    }});
    
    remoteRig.run();
    remoteRig.await();
    remoteRig.close();
    return remoteRig.getSummary();
  }
  
  public static void main(String[] args) throws Exception {
    BashInteractor.Ulimit.main(null);
    final URI uri = new URI(URL);
    LOG_STREAM.format("_\nRemote benchmark started (URI: %s, initiate: %b)...\n", 
                      uri, INITIATE);
    new Config() {{
      runner = RemoteRigBenchmark::run;
      host = uri.getHost();
      port = uri.getPort();
      path = uri.getPath();
      syncFrames = SYNC_FRAMES;
      topicSpec = TopicLibrary.largeLeaves();
      initiate = INITIATE;
      normalMinNanos = NORMAL_MIN;
      log = new LogConfig() {{
        progress = intermediateSummaries = false;
        stages = true;
        summary = true;
      }};
    }}.test();
  }
}
