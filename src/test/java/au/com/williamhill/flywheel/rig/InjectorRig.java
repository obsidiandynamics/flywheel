package au.com.williamhill.flywheel.rig;

import static au.com.williamhill.flywheel.util.SocketTestSupport.*;

import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import org.eclipse.jetty.util.*;

import com.google.gson.*;
import com.obsidiandynamics.indigo.benchmark.*;
import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.remote.*;
import au.com.williamhill.flywheel.rig.Announce.*;
import au.com.williamhill.flywheel.topic.*;
import au.com.williamhill.flywheel.util.*;

public final class InjectorRig extends Thread implements TestSupport, AutoCloseable, RemoteNexusHandler {
  private static final String CONTROL_TOPIC = "control";
  
  public static class InjectorRigConfig {
    URI uri;
    TopicSpec topicSpec;
    int pulseDurationMillis;
    int pulses;
    int warmupPulses;
    boolean text;
    int bytes;
    LogConfig log;
    
    static URI getUri(String host, int port, String path) throws URISyntaxException, MalformedURLException {
      return new URL("http", host, port, path).toURI();
    }
  }
  
  private enum State {
    CONNECT_WAIT, RUNNING, STOPPED, CLOSING, CLOSED
  }
  
  private final RemoteNode node;
  
  private final InjectorRigConfig config;
  
  private final List<Topic> leafTopics;
  
  private final Gson subframeGson = new Gson();
  
  private final Set<String> controlSessions = new ConcurrentHashSet<>();
  
  private final Set<String> confirmedWaits = new ConcurrentHashSet<>();
  
  private final Map<String, AtomicInteger> subscriptionsByNode = new ConcurrentHashMap<>();
  
  private RemoteNexus nexus;
  
  private volatile State state = State.CONNECT_WAIT;
  
  private volatile long took;
  
  public InjectorRig(RemoteNode node, InjectorRigConfig config) {
    super("InjectorRig");
    this.node = node;
    this.config = config;
    
    leafTopics = config.topicSpec.getLeafTopics();
    try {
      openNexus();
    } catch (Exception e) {
      e.printStackTrace(config.log.out);
      throw new RuntimeException(e);
    }
    start();
  }
  
  private void openNexus() throws Exception {
    final String sessionId = generateSessionId();
    if (config.log.stages) config.log.out.format("i: opening nexus (%s)...\n", sessionId);
    nexus = node.open(config.uri, this);
    nexus.bind(new BindFrame(UUID.randomUUID(), sessionId, null, 
                             new String[]{CONTROL_TOPIC + "/#"}, new String[]{}, null)).get();
  }
  
  private String generateSessionId() {
    return Long.toHexString(Crypto.machineRandom());
  }
  
  long getTimeTaken() {
    return took;
  }
  
  @Override
  public void run() {
    while (state != State.CLOSING) {
      runBenchmark();
      TestSupport.sleep(10);
    }
  }
  
  private void runBenchmark() {
    if (state == State.RUNNING) {
      if (config.log.stages) config.log.out.format("i: benchmark commenced on %s\n", new Date());
    } else {
      return;
    }

    int perInterval = Math.max(1, leafTopics.size() / config.pulseDurationMillis);
    int interval = 1;
    
    int pulse = 0;
    if (config.log.stages) config.log.out.format("i: warming up (%,d pulses)...\n", config.warmupPulses);
    boolean warmup = true;
    final byte[] binPayload = config.text ? null : randomBytes(config.bytes);
    final String textPayload = config.text ? randomString(config.bytes) : null;
    final int progressInterval = Math.max(1, config.pulses / 25);
    final long start = System.currentTimeMillis();
    
    outer: while (state == State.RUNNING) {
      final long cycleStart = System.nanoTime();
      int sent = 0;
      for (Topic t : leafTopics) {
        if (warmup && pulse >= config.warmupPulses) {
          warmup = false;
          if (config.log.stages) config.log.out.format("i: starting timed run (%,d pulses)...\n", 
                                                       config.pulses - config.warmupPulses);
        }
        final long timestamp = warmup ? 0 : System.nanoTime();
        if (config.text) {
          final String str = new StringBuilder().append(timestamp).append(' ').append(textPayload).toString();
          nexus.publish(new PublishTextFrame(t.toString(), str));
        } else {
          final ByteBuffer buf = ByteBuffer.allocate(8 + config.bytes);
          buf.putLong(timestamp);
          buf.put(binPayload);
          buf.flip();
          nexus.publish(new PublishBinaryFrame(t.toString(), BinaryUtils.toByteArray(buf)));
        }
        
        if (sent++ % perInterval == 0) {
          try {
            Thread.sleep(interval);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            continue outer;
          }
        }
      }
      final long cycleTook = System.nanoTime() - cycleStart;
      if (cycleTook > config.pulseDurationMillis * 1_000_000l) {
        if (interval > 1) {
          interval--;
        } else {
          perInterval++;
        }
      } else {
        if (perInterval > 1) {
          perInterval--;
        } else {
          interval++;
        }
      }
      if (config.log.verbose) config.log.out.format("i: pulse %,d took %,d (%,d every %,d ms)\n", 
                                                    pulse, cycleTook, perInterval, interval);
      
      if (config.log.progress && pulse % progressInterval == 0) {
        config.log.printProgressBlock();
      }
      
      if (++pulse == config.pulses) {
        break;
      }
    }
    took = System.currentTimeMillis() - start; 
    
    state = State.STOPPED;
    
    awaitRemotes();
  }
  
  private static String getControlRxTopic(String remoteId) {
    return CONTROL_TOPIC + "/" + remoteId + "/rx";
  }
  
  private void awaitRemotes() {
    for (String controlSessionId : controlSessions) {
      final int subscribers = getSubscribers(controlSessionId);
      final long expectedMessages = (long) config.pulses * subscribers;
      
      if (config.log.stages) config.log.out.format("i: awaiting remote %s (%,d messages across %,d subscribers)...\n",
                                                   controlSessionId, expectedMessages, subscribers);
      
      pubToControl(controlSessionId, new Wait(expectedMessages));
    }
    
    try {
      Await.boundedTimeout(60_000, () -> controlSessions.size() == confirmedWaits.size());
    } catch (InterruptedException e) {
      e.printStackTrace(config.log.out);
      Thread.currentThread().interrupt();
    } catch (TimeoutException e) {
      config.log.out.format("e: timed out waiting for remote\n");
    }
  }
  
  public boolean await() throws InterruptedException {
    Await.perpetual(() -> state == State.STOPPED);
    return true;
  }
  
  @Override
  public void close() throws Exception {
    final boolean wasStopped = state == State.STOPPED;
    state = State.CLOSING;
    if (! wasStopped) {
      interrupt();
    }
    join();
    
    closeNexuses();
    node.close();
    state = State.CLOSED;
  }
  
  private void closeNexuses() throws Exception, InterruptedException {
    final List<RemoteNexus> nexuses = node.getNexuses();
    if (nexuses.isEmpty()) return;
    
    if (config.log.stages) config.log.out.format("i: closing nexuses (%,d)...\n", nexuses.size());
    for (RemoteNexus nexus : nexuses) {
      nexus.close();
    }
    for (RemoteNexus nexus : nexuses) {
      if (! nexus.awaitClose(60_000)) {
        config.log.out.format("i: timed out while waiting for close of %s\n", nexus);
      }
    }
  }
  
  private void addSubscriber(String sessionId) {
    synchronized (subscriptionsByNode) {
      AtomicInteger counter = subscriptionsByNode.get(sessionId);
      if (counter == null) {
        subscriptionsByNode.put(sessionId, counter = new AtomicInteger());
      }
      counter.incrementAndGet();
    }
  }
  
  int getTotalSubscribers() {
    return subscriptionsByNode.values().stream().collect(Collectors.summingInt(v -> v.get())).intValue();
  }
  
  private int getSubscribers(String sessionId) {
    return subscriptionsByNode.get(sessionId).get();
  }
  
  private void pubToControl(String sessionId, RigSubframe subframe) {
    nexus.publish(new PublishTextFrame(getControlRxTopic(sessionId), subframe.marshal(subframeGson)));
  }

  @Override
  public void onOpen(RemoteNexus nexus) {
    if (config.log.verbose) config.log.out.format("i: opened %s\n", nexus);
  }

  @Override
  public void onClose(RemoteNexus nexus) {
    if (config.log.verbose) config.log.out.format("i: closed %s\n", nexus);
  }

  @Override
  public void onText(RemoteNexus nexus, String topic, String payload) {
    if (topic.startsWith(CONTROL_TOPIC)) {
      final Topic t = Topic.of(topic);
      final String sessionId = t.getParts()[1];
      final RigSubframe subframe = RigSubframe.unmarshal(payload, subframeGson);
      if (topic.endsWith("/tx")) {
        onTxSubframe(nexus, sessionId, subframe);
      } else {
        onRxSubframe(nexus, sessionId, subframe);
      }
    }
  }

  private void onTxSubframe(RemoteNexus nexus, String sessionId, RigSubframe subframe) {
    if (config.log.verbose) config.log.out.format("i: subframe %s %s\n", sessionId, subframe);
    if (subframe instanceof Announce) {
      final Announce announce = (Announce) subframe;
      if (announce.getRole() == Role.CONTROL) {
        controlSessions.add(sessionId);
      } else {
        addSubscriber(announce.getControlSessionId());
      }
    } else if (subframe instanceof Sync) {
      pubToControl(sessionId, new SyncResponse(System.nanoTime()));
    } else if (subframe instanceof Begin) {
      state = State.RUNNING;
    } else {
      config.log.out.format("ERROR: Unsupported subframe of type %s\n", subframe.getClass().getName());
    }
  }
  
  private void onRxSubframe(RemoteNexus nexus, String sessionId, RigSubframe subframe) {
    if (subframe instanceof Wait) {
      confirmedWaits.add(sessionId);
    }
  }

  @Override
  public void onBinary(RemoteNexus nexus, String topic, byte[] payload) {
    if (config.log.verbose) config.log.out.format("i: pub %s\n", nexus);
  }
}
