package au.com.williamhill.flywheel.edge.backplane.kafka;

import java.util.*;

import org.junit.*;

import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.util.*;

public class ScramjetKafkaBackplaneTest extends BackplaneTest {
  private static final String TOPIC_NAME = "flywheel.backplane.v1";
  private static final int CYCLES = 2;
  private static final int SCALE = 1;
  
  private Kafka<String, KafkaData> kafka;
  
  private final Map<String, KafkaBackplane> backplanes = new HashMap<>();
  
  @Override
  protected void init() throws Exception {
    super.init();
    kafka = getKafka();
  }
  
  protected Kafka<String, KafkaData> getKafka() {
    return new MockKafka<>(1, Integer.MAX_VALUE);
  }
  
  @Override
  protected void cleanup() throws Exception {
    for (KafkaBackplane backplane : backplanes.values()) {
      backplane.close();
    }
    backplanes.clear();
    super.cleanup();
  }

  @Override
  protected Backplane getBackplane(String clusterId, String brokerId) throws Exception {
    final KafkaBackplaneConfig config = new KafkaBackplaneConfig() {{
      topic = TOPIC_NAME;
      kafka = ScramjetKafkaBackplaneTest.this.kafka;
      serializer = ScramjetSerializer.class;
      deserializer = ScramjetDeserializer.class;
      pollTimeoutMillis = 1;
    }};
    return Keyed.getOrSet(backplanes, backplanes, brokerId, () -> new KafkaBackplane(config, clusterId, brokerId));
  }
  
  @Test
  public final void testSingleNode() throws Exception {
    final int connectors = 1;
    final int topics = 3;
    final int messagesPerTopic = 10 * SCALE;
    final int expectedPartitions = (connectors - 1) * topics;
    final int expectedMessages = expectedPartitions * messagesPerTopic;
    test(CYCLES, false, connectors, topics, messagesPerTopic, expectedPartitions, expectedMessages);
    test(CYCLES, true, connectors, topics, messagesPerTopic, expectedPartitions, expectedMessages);
  }
  
  @Test
  public final void testMultiNode() throws Exception {
    final int connectors = 4;
    final int topics = 3;
    final int messagesPerTopic = 10 * SCALE;
    final int expectedPartitions = (connectors - 1) * topics;
    final int expectedMessages = expectedPartitions * messagesPerTopic;
    test(CYCLES, false, connectors, topics, messagesPerTopic, expectedPartitions, expectedMessages);
    test(CYCLES, true, connectors, topics, messagesPerTopic, expectedPartitions, expectedMessages);
  }
}
