package au.com.williamhill.flywheel.edge.plugin.beacon;

import java.text.*;
import java.util.*;

import org.slf4j.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;

/**
 *  A plugin that repeatedly publishes the current time on a set topic.
 */
@Y
public final class Beacon implements Plugin {
  private static final Logger LOG = LoggerFactory.getLogger(Beacon.class);

  private final class BeaconRunner extends Thread implements TopicListener {
    private final EdgeNode edge;

    private final DateFormat dateFormat = new SimpleDateFormat(format);

    private volatile boolean running = true;

    BeaconRunner(EdgeNode edge) throws Exception {
      super("BeaconRunner");
      this.edge = edge;
      edge.addTopicListener(this);
      if (LOG.isDebugEnabled()) LOG.debug("Starting plugin Beacon:\n" +
          "  interval {} ms\n" +
          "  topic: {}\n" +
          "  format: {}",
          intervalMillis, topic, format);
      start();
    }

    @Override
    public void run() {
      while (running) {
        final String message = dateFormat.format(new Date());
        edge.publish(topic, message);
        try {
          Thread.sleep(intervalMillis);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }

    @Override
    public void onOpen(EdgeNexus nexus) {
      if (LOG.isDebugEnabled()) LOG.debug("{}: opened", nexus);
    }

    @Override
    public void onClose(EdgeNexus nexus) {
      if (LOG.isDebugEnabled()) LOG.debug("{}: closed", nexus);
    }

    @Override
    public void onBind(EdgeNexus nexus, BindFrame bind, BindResponseFrame bindRes) {
      if (LOG.isDebugEnabled()) LOG.debug("{}: bind {} -> {}", nexus, bind, bindRes);
    }

    @Override
    public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {
      if (LOG.isDebugEnabled()) LOG.debug("{}: publish {}", nexus, pub);
    }

    @Override
    public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
      if (LOG.isDebugEnabled()) LOG.debug("{}: publish {}", nexus, pub);
    }

    void dispose() throws InterruptedException {
      if (LOG.isDebugEnabled()) LOG.debug("Stopping plugin Beacon");
      running = false;
      interrupt();
      join();
    }
  }

  private final int intervalMillis;

  private final String topic;

  private final String format;

  private BeaconRunner runner;

  public Beacon() {
    this(1000, "time", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  }

  public Beacon(@YInject(name="intervalMillis") int intervalMillis, 
                @YInject(name="topic") String topic, 
                @YInject(name="format") String format) {
    this.intervalMillis = intervalMillis;
    this.topic = topic;
    this.format = format;
  }

  @Override
  public void onBuild(EdgeNodeBuilder builder) throws Exception {
    // Nothing to do; this plugin doesn't require intervention at the build stage.
  }

  @Override
  public void onRun(EdgeNode edge) throws Exception {
    runner = new BeaconRunner(edge);
  }

  @Override
  public void close() throws Exception {
    runner.dispose();
  }

  @Override
  public String toString() {
    return "Beacon {interval: " + intervalMillis + " ms, topic: " + topic + ", format: " + format + "}";
  }
}
