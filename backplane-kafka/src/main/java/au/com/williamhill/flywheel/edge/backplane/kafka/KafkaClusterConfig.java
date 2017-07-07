package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;

public class KafkaClusterConfig {
  public String bootstrapServers;
  public String prodAcks = "all";
  public int prodRetries = 0;
  public int prodBatchSize = 16_384;
  public int prodLingerMillis = 1;
  public int prodBufferMemory = 33_554_432;
  public boolean consAutoCommit = true;
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
