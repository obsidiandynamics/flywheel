package au.com.williamhill.flywheel.rig;

import static com.obsidiandynamics.indigo.util.PropertyUtils.*;

import java.net.*;

import com.obsidiandynamics.indigo.benchmark.*;
import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.remote.*;
import au.com.williamhill.flywheel.rig.DoubleRigBenchmark.*;
import au.com.williamhill.flywheel.rig.InjectorRig.*;
import au.com.williamhill.flywheel.topic.*;

public final class InjectorRigBenchmark implements TestSupport {
  private static final String URL = get("flywheel.rig.url", String::valueOf, "ws://localhost:8080/broker");
  private static final int PULSES = get("flywheel.rig.pulses", Integer::valueOf, 30);
  private static final int PULSE_DURATION = get("flywheel.rig.pulseDuration", Integer::valueOf, 1000);
  private static final float WARMUP_FRAC = get("flywheel.rig.warmupFrac", Float::valueOf, 0.10f);
  private static final boolean TEXT = get("flywheel.rig.text", Boolean::valueOf, false);
  private static final int BYTES = get("flywheel.rig.bytes", Integer::valueOf, 128);
  private static final boolean CYCLE = get("flywheel.rig.cycle", Boolean::valueOf, false);
  
  private static Summary run(Config c) throws Exception {
    final RemoteNode remote = RemoteNode.builder()
        .build();
    final InjectorRig injectorRig = new InjectorRig(remote, new InjectorRigConfig() {{
      uri = getUri(c.host, c.port, c.path);
      topicSpec = c.topicSpec;
      pulseDurationMillis = c.pulseDurationMillis;
      pulses = c.pulses;
      warmupPulses = c.warmupPulses;
      text = c.text;
      bytes = c.bytes;
      log = c.log;
    }});
    
    injectorRig.await();
    injectorRig.close();
    LOG_STREAM.println("Injector benchmark completed");
    
    final Summary summary = new Summary();
    summary.compute(new Elapsed() {
      @Override public long getTotalProcessed() {
        return (long) injectorRig.getTotalSubscribers() * c.pulses;
      }

      @Override public long getTimeTaken() {
        return injectorRig.getTimeTaken();
      }
    });
    return summary;
  }
  
  public static void main(String[] args) throws Exception {
    BashInteractor.Ulimit.main(null);
    final URI uri = new URI(URL);
    do {
      LOG_STREAM.format("_\nInjector benchmark started (URI: %s)...\n", uri);
      new Config() {{
        runner = InjectorRigBenchmark::run;
        host = uri.getHost();
        port = uri.getPort();
        path = uri.getPath();
        pulses = PULSES;
        pulseDurationMillis = PULSE_DURATION;
        topicSpec = TopicLibrary.jumboLeaves();
        warmupFrac = WARMUP_FRAC;
        text = TEXT;
        bytes = BYTES;
        log = new LogConfig() {{
          progress = intermediateSummaries = false;
          stages = true;
          summary = true;
        }};
      }}.test();
    } while (CYCLE);
  }
}