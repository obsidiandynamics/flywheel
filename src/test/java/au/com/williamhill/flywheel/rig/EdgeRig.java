package au.com.williamhill.flywheel.rig;

import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import com.google.gson.*;
import com.obsidiandynamics.await.*;
import com.obsidiandynamics.indigo.benchmark.*;
import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.socketx.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.rig.Announce.*;
import au.com.williamhill.flywheel.topic.*;

public final class EdgeRig extends Thread implements TestSupport, AutoCloseable, TopicListener {
  private static final String CONTROL_TOPIC = "control";
  
  public static class EdgeRigConfig {
    TopicSpec topicSpec;
    int pulseDurationMillis;
    int pulses;
    int warmupPulses;
    boolean text;
    int bytes;
    LogConfig log;
  }
  
  private enum State {
    CONNECT_WAIT, RUNNING, STOPPED, CLOSING, CLOSED
  }
  
  private final EdgeNode node;
  
  private final EdgeRigConfig config;
  
  private final List<Topic> leafTopics;
  
  private final Gson subframeGson = new Gson();
  
  private final Set<String> controlSessions = ConcurrentHashMap.newKeySet();
  
  private final Map<String, AtomicInteger> subscriptionsByNode = new ConcurrentHashMap<>();
  
  private final Object subscriptionsLock = new Object();
  
  private volatile State state = State.CONNECT_WAIT;
  
  private volatile long took;
  
  public EdgeRig(EdgeNode node, EdgeRigConfig config) {
    super("EdgeRig");
    this.node = node;
    this.config = config;
    
    leafTopics = config.topicSpec.getLeafTopics();
    node.addTopicListener(this);
    start();
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
      if (config.log.stages) config.log.out.format("e: benchmark commenced on %s\n", new Date());
    } else {
      return;
    }

    int perInterval = Math.max(1, leafTopics.size() / config.pulseDurationMillis);
    int interval = 1;
    
    int pulse = 0;
    if (config.log.stages) config.log.out.format("e: warming up (%,d pulses)...\n", config.warmupPulses);
    boolean warmup = true;
    final byte[] binPayload = config.text ? null : BinaryUtils.randomBytes(config.bytes);
    final String textPayload = config.text ? BinaryUtils.randomHexString(config.bytes) : null;
    final int progressInterval = Math.max(1, config.pulses / 25);
    final long start = System.currentTimeMillis();
    
    outer: while (state == State.RUNNING) {
      final long cycleStart = System.nanoTime();
      int sent = 0;
      for (Topic t : leafTopics) {
        if (warmup && pulse >= config.warmupPulses) {
          warmup = false;
          if (config.log.stages) config.log.out.format("e: starting timed run (%,d pulses)...\n", 
                                                       config.pulses - config.warmupPulses);
        }
        final long timestamp = warmup ? 0 : System.nanoTime();
        if (config.text) {
          final String str = new StringBuilder().append(timestamp).append(' ').append(textPayload).toString();
          node.publish(t.toString(), str);
        } else {
          final ByteBuffer buf = ByteBuffer.allocate(8 + config.bytes);
          buf.putLong(timestamp);
          buf.put(binPayload);
          buf.flip();
          node.publish(t.toString(), BinaryUtils.toByteArray(buf));
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
      if (config.log.verbose) config.log.out.format("e: pulse %,d took %,d (%,d every %,d ms)\n", 
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
      
      if (config.log.stages) config.log.out.format("e: awaiting remote %s (%,d messages across %,d subscribers)...\n",
                                                   controlSessionId, expectedMessages, subscribers);
      
      pubToControl(controlSessionId, new Wait(expectedMessages));
    }
    
    try {
      Await.boundedTimeout(60_000, () -> controlSessions.isEmpty());
    } catch (InterruptedException e) {
      e.printStackTrace(config.log.out);
      Thread.currentThread().interrupt();
    } catch (TimeoutException e) {
      config.log.out.format("e: timed out waiting for remote\n");
    }
  }
  
  public boolean await() throws InterruptedException {
    Await.perpetual(() -> state == State.STOPPED && node.getNexuses().isEmpty());
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
  
  private void addSubscriber(String sessionId) {
    synchronized (subscriptionsLock) {
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

  @Override
  public void onOpen(EdgeNexus nexus) {
    if (config.log.verbose) config.log.out.format("e: opened %s\n", nexus);
  }

  @Override
  public void onClose(EdgeNexus nexus) {
    if (config.log.verbose) config.log.out.format("e: closed %s\n", nexus);
    final String sessionId = nexus.getSession().getSessionId();
    controlSessions.remove(sessionId);
  }

  @Override
  public void onBind(EdgeNexus nexus, BindFrame bind, BindResponseFrame bindRes) {
    if (config.log.verbose) config.log.out.format("e: sub %s %s\n", nexus, bind);
  }
  
  @Override
  public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {
    if (! nexus.isLocal() && pub.getTopic().startsWith(CONTROL_TOPIC)) {
      final Topic t = Topic.of(pub.getTopic());
      final String sessionId = t.getParts()[1];
      final RigSubframe subframe = RigSubframe.unmarshal(pub.getPayload(), subframeGson);
      onSubframe(nexus, sessionId, subframe);
    }
  }
  
  private void onSubframe(EdgeNexus nexus, String sessionId, RigSubframe subframe) {
    if (config.log.verbose) config.log.out.format("e: subframe %s %s\n", sessionId, subframe);
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
  
  private void pubToControl(String sessionId, RigSubframe subframe) {
    node.publish(getControlRxTopic(sessionId), subframe.marshal(subframeGson));
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
    if (config.log.verbose) config.log.out.format("e: pub %s %s\n", nexus, pub);
  }
}
