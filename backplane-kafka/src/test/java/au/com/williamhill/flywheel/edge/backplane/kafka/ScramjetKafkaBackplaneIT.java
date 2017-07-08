package au.com.williamhill.flywheel.edge.backplane.kafka;

import static com.obsidiandynamics.indigo.util.PropertyUtils.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.util.*;

public final class ScramjetKafkaBackplaneIT extends ScramjetKafkaBackplaneTest {
  private static final boolean SETUP_TEARDOWN = true;
  private static final String COMPOSE_FILE = "stacks/kafka/docker-compose.yaml";
  private static final String BROKERS = get("flywheel.backplane.kafka.brokers", String::valueOf, "localhost:9092");
  
  @BeforeClass
  public static void beforeClass() throws Exception {
    if (SETUP_TEARDOWN) {
      TestSupport.logStatic("Starting Kafka stack... ");
      final long took = TestSupport.tookThrowing(() -> {
        DockerUtils.checkInstalled();
        DockerUtils.composeUp(COMPOSE_FILE);
      });
      TestSupport.logStatic("took %,d ms\n", took);
    }
  }
  
  @AfterClass
  public static void afterClass() throws Exception {
    if (SETUP_TEARDOWN) {
      TestSupport.logStatic("Stopping Kafka stack... ");
      final long took = TestSupport.tookThrowing(() -> {
        DockerUtils.composeDown(COMPOSE_FILE);
      });
      TestSupport.logStatic("took %,d ms\n", took);
    }
  }
  
  @Override
  protected Kafka<String, KafkaData> getKafka() {
    return new KafkaCluster<>(new KafkaClusterConfig() {{
      bootstrapServers = BROKERS;
    }});
  }
}
