package au.com.williamhill.flywheel.edge.plugin.beacon;

import java.text.*;
import java.util.*;

import org.slf4j.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.plugin.*;

/**
 *  A plugin that repeatedly publishes the current time on a set topic.
 */
@Y
public final class Beacon implements Plugin {
  private final class BeaconRunner extends Thread {
    private final EdgeNode edge;

    private final DateFormat dateFormat = new SimpleDateFormat(format);

    private volatile boolean running = true;

    BeaconRunner(EdgeNode edge) throws Exception {
      super("BeaconRunner");
      this.edge = edge;
      logger.debug("Starting plugin Beacon:\n" +
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
        }
      }
    }

    void dispose() throws InterruptedException {
      logger.debug("Stopping plugin Beacon");
      running = false;
      interrupt();
      join();
    }
  }
  
  @YInject
  private int intervalMillis = 1000;

  @YInject
  private String topic = "time";

  @YInject
  private String format = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  
  private Logger logger = LoggerFactory.getLogger(Beacon.class);

  private BeaconRunner runner;
  
  public Beacon withLogger(Logger logger) {
    this.logger = logger;
    return this;
  }
  
  public Beacon withInterval(int intervalMillis) {
    this.intervalMillis = intervalMillis;
    return this;
  }

  public Beacon withTopic(String topic) {
    this.topic = topic;
    return this;
  }

  public Beacon withFormat(String format) {
    this.format = format;
    return this;
  }

  @Override
  public void onBuild(EdgeNodeBuilder builder) throws Exception {}

  @Override
  public void onRun(EdgeNode edge) throws Exception {
    runner = new BeaconRunner(edge);
  }

  @Override
  public void close() throws InterruptedException {
    runner.dispose();
  }

  @Override
  public String toString() {
    return "Beacon {interval: " + intervalMillis + " ms, topic: " + topic + ", format: " + format + "}";
  }
}
