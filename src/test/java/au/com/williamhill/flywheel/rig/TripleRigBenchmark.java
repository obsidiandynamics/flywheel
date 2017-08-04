package au.com.williamhill.flywheel.rig;

import java.util.*;

import org.awaitility.*;
import org.junit.*;

import com.obsidiandynamics.indigo.benchmark.*;
import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.remote.*;
import au.com.williamhill.flywheel.rig.InjectorRig.*;
import au.com.williamhill.flywheel.rig.RemoteRig.*;
import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.topic.*;
import au.com.williamhill.flywheel.util.*;

public final class TripleRigBenchmark implements TestSupport {
  private static final String HOST = "localhost";
  private static final int PREFERRED_PORT = 8080;
  private static final String PATH = "/broker";
  
  abstract static class Config implements Spec {
    static {
      Awaitility.doNotCatchUncaughtExceptionsByDefault();
    }
    
    ThrowingFunction<Config, Summary> runner = TripleRigBenchmark::test;
    String host;
    int port;
    String path;
    int pulses;
    int pulseDurationMillis;
    int injectors;
    int syncFrames;
    TopicSpec topicSpec;
    boolean initiate;
    boolean text;
    int bytes;
    double normalMinNanos = Double.NaN;
    int statsPeriod;
    float warmupFrac;
    LogConfig log;
    
    /* Derived fields. */
    int warmupPulses;
    
    private boolean initialised;
    
    @Override
    public void init() {
      if (initialised) return;
      
      warmupPulses = (int) (pulses * warmupFrac);
      initialised = true;
    }

    @Override
    public LogConfig getLog() {
      return log;
    }

    @Override
    public String describe() {
      return String.format("%,d pulses, %,d ms duration, %.0f%% warmup fraction",
                           pulses, pulseDurationMillis, warmupFrac * 100);
    }
    
    SpecMultiplier applyDefaults() {
      host = HOST;
      port = SocketTestSupport.getAvailablePort(PREFERRED_PORT);
      path = PATH;
      warmupFrac = 0.05f;
      initiate = true;
      injectors = 2;
      normalMinNanos = 50_000f;
      statsPeriod = 1;
      log = new LogConfig() {{
        summary = stages = LOG;
        verbose = false;
      }};
      return times(2);
    }

    @Override
    public Summary run() throws Exception {
      return runner.apply(this);
    }
  }

  @Test
  public void testTextSmallSingleton() throws Exception {
    new Config() {{
      pulses = 10;
      pulseDurationMillis = 1;
      syncFrames = 10;
      topicSpec = TopicLibrary.singleton(10);
      text = true;
      bytes = 16;
    }}.applyDefaults().test();
  }

  @Test
  public void testBinarySmallSingleton() throws Exception {
    new Config() {{
      pulses = 10;
      pulseDurationMillis = 1;
      syncFrames = 10;
      topicSpec = TopicLibrary.singleton(10);
      text = false;
      bytes = 16;
    }}.applyDefaults().test();
  }

  @Test
  public void testTextSmallLeaves() throws Exception {
    new Config() {{
      pulses = 10;
      pulseDurationMillis = 1;
      syncFrames = 10;
      topicSpec = TopicLibrary.smallLeaves();
      text = true;
      bytes = 16;
    }}.applyDefaults().test();
  }

  @Test
  public void testBinarySmallLeaves() throws Exception {
    new Config() {{
      pulses = 10;
      pulseDurationMillis = 1;
      syncFrames = 10;
      topicSpec = TopicLibrary.smallLeaves();
      text = false;
      bytes = 16;
    }}.applyDefaults().test();
  }

  private static Summary test(Config c) throws Exception {
    final EdgeNode edge = EdgeNode.builder()
        .withServerConfig(new XServerConfig() {{ port = c.port; path = c.path; }})
        .build();
    
    final RemoteNode injectorNode = RemoteNode.builder()
        .build();
    final InjectorRig injectorRig = new InjectorRig(injectorNode, new InjectorRigConfig() {{
      uri = getUri(c.host, c.port, c.path);
      topicSpec = c.topicSpec;
      pulseDurationMillis = c.pulseDurationMillis;
      pulses = c.pulses;
      injectors = c.injectors;
      warmupPulses = c.warmupPulses;
      text = c.text;
      bytes = c.bytes;
      log = c.log;
    }});
    
    final RemoteNode remote = RemoteNode.builder()
        .build();
    final RemoteRig remoteRig = new RemoteRig(remote, new RemoteRigConfig() {{
      topicSpec = c.topicSpec;
      syncFrames = c.syncFrames;
      uri = getUri(c.host, c.port, c.path);
      initiate = c.initiate;
      normalMinNanos = c.normalMinNanos;
      statsPeriod = c.statsPeriod;
      log = c.log;
    }});

    try {
      remoteRig.run();
      remoteRig.await();
    } finally {
      injectorRig.close();
      remoteRig.close();
      closeEdgeNexuses(c, edge);
      edge.close();
    }
    
    return remoteRig.getSummary();
  }
  
  private static void closeEdgeNexuses(Config config, EdgeNode node) throws Exception, InterruptedException {
    final List<EdgeNexus> nexuses = node.getNexuses();
    if (nexuses.isEmpty()) return;
    
    if (config.log.stages) config.log.out.format("e: closing nexuses (%,d)...\n", nexuses.size());
    for (EdgeNexus nexus : nexuses) {
      nexus.close();
    }
    for (EdgeNexus nexus : nexuses) {
      if (! nexus.awaitClose(60_000)) {
        config.log.out.format("e: timed out while waiting for close of %s\n", nexus);
      }
    }
  }
  
  /**
   *  Run with -XX:-MaxFDLimit -Xms2G -Xmx4G -XX:+UseConcMarkSweepGC
   *  
   *  @param args Arguments.
   *  @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    BashInteractor.Ulimit.main(null);
    new Config() {{
      host = HOST;
      port = SocketTestSupport.getAvailablePort(PREFERRED_PORT);
      path = PATH;
      pulses = 300;
      pulseDurationMillis = 100;
      injectors = 10;
      syncFrames = 0;
      topicSpec = TopicLibrary.largeLeaves();
      warmupFrac = 0.20f;
      initiate = true;
      normalMinNanos = Double.NaN;
      statsPeriod = 1;
      text = false;
      bytes = 128;
      log = new LogConfig() {{
        progress = intermediateSummaries = true;
        stages = false;
        summary = true;
      }};
    }}.testPercentile(1, 5, 50, Summary::byLatency);
  }
}
