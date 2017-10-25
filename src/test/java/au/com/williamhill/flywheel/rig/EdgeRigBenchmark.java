package au.com.williamhill.flywheel.rig;

import static com.obsidiandynamics.indigo.util.PropertyUtils.*;

import java.util.*;

import com.obsidiandynamics.indigo.benchmark.*;
import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.shell.*;
import com.obsidiandynamics.socketx.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.rig.DoubleRigBenchmark.*;
import au.com.williamhill.flywheel.rig.EdgeRig.*;
import au.com.williamhill.flywheel.topic.*;

public final class EdgeRigBenchmark implements TestSupport {
  private static final Properties PROPS = new Properties(System.getProperties());
  private static final int PORT = getOrSet(PROPS, "flywheel.rig.port", Integer::valueOf, 8080);
  private static final String PATH = getOrSet(PROPS, "flywheel.rig.path", String::valueOf, "/broker");
  private static final int PULSES = getOrSet(PROPS, "flywheel.rig.pulses", Integer::valueOf, 30);
  private static final int PULSE_DURATION = getOrSet(PROPS, "flywheel.rig.pulseDuration", Integer::valueOf, 1000);
  private static final String SPEC = getOrSet(PROPS, "flywheel.rig.spec", String::valueOf, "cp://specs/jumbo-leaves.yaml");
  private static final float WARMUP_FRAC = getOrSet(PROPS, "flywheel.rig.warmupFrac", Float::valueOf, 0.10f);
  private static final boolean TEXT = getOrSet(PROPS, "flywheel.rig.text", Boolean::valueOf, true);
  private static final int BYTES = getOrSet(PROPS, "flywheel.rig.bytes", Integer::valueOf, 128);
  private static final boolean CYCLE = getOrSet(PROPS, "flywheel.rig.cycle", Boolean::valueOf, false);
  
  private static Summary run(Config c) throws Exception {
    final EdgeNode edge = EdgeNode.builder()
        .withServerConfig(new XServerConfig() {{ port = c.port; path = c.path; }})
        .build();
    final EdgeRig edgeRig = new EdgeRig(edge, new EdgeRigConfig() {{
      topicSpec = c.topicSpec;
      pulseDurationMillis = c.pulseDurationMillis;
      pulses = c.pulses;
      warmupPulses = c.warmupPulses;
      text = c.text;
      bytes = c.bytes;
      log = c.log;
    }});
    
    edgeRig.await();
    edgeRig.close();
    LOG_STREAM.println("Edge benchmark completed");
    
    final Summary summary = new Summary();
    summary.compute(new Elapsed() {
      @Override public long getTotalProcessed() {
        return (long) edgeRig.getTotalSubscribers() * c.pulses;
      }

      @Override public long getTimeTaken() {
        return edgeRig.getTimeTaken();
      }
    });
    return summary;
  }
  
  public static void main(String[] args) throws Exception {
    BourneUtils.run("ulimit -Sa", null, true, System.out::print);
    LOG_STREAM.println();
    filter("flywheel.rig", PROPS).entrySet().stream()
    .map(e -> String.format("%-30s: %s", e.getKey(), e.getValue())).forEach(LOG_STREAM::println);
    
    do {
      LOG_STREAM.println("_\nEdge benchmark started; waiting for remote connections...");
      new Config() {{
        runner = EdgeRigBenchmark::run;
        port = PORT;
        path = PATH;
        pulses = PULSES;
        pulseDurationMillis = PULSE_DURATION;
        topicSpec = TopicLibrary.load(SPEC);
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