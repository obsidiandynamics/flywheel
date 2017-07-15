package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;

import com.obsidiandynamics.yconf.*;

@Y
public class KafkaClusterConfig {
  @YInject
  public String bootstrapServers;

  @YInject
  public String prodAcks = "all";
  
  @YInject
  public int prodRetries = 0;
  
  @YInject
  public int prodBatchSize = 16_384;
  
  @YInject
  public int prodLingerMillis = 1;
  
  @YInject
  public int prodBufferMemory = 33_554_432;
  
  @YInject
  public boolean consAutoCommit = true;
  
  @YInject
  public int consAutoCommitIntervalMillis = 100;
  
  void init() {
    if (bootstrapServers == null) throw new IllegalArgumentException("Must specify a value for 'bootstrapServers'");
  }
  
  Properties getCommonProps() {
    final Properties props = new Properties();
    props.setProperty("bootstrap.servers", bootstrapServers);
    return props;
  }

  Properties getProducerProps() {
    final Properties props = getCommonProps();
    props.setProperty("acks", prodAcks);
    props.setProperty("retries", String.valueOf(prodRetries));
    props.setProperty("batch.size", String.valueOf(prodBatchSize));
    props.setProperty("linger.ms", String.valueOf(prodLingerMillis));
    props.setProperty("buffer.memory", String.valueOf(prodBufferMemory));
    return props;
  }
  
  Properties getConsumerProps() {
    final Properties props = getCommonProps();
    props.setProperty("enable.auto.commit", String.valueOf(consAutoCommit));
    props.setProperty("auto.commit.interval.ms", String.valueOf(consAutoCommitIntervalMillis));
    return props;
  }
}
