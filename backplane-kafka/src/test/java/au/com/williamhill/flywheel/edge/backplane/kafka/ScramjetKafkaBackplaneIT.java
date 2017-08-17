package au.com.williamhill.flywheel.edge.backplane.kafka;

import static com.obsidiandynamics.indigo.util.PropertyUtils.*;

import org.junit.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.util.*;

public final class ScramjetKafkaBackplaneIT extends ScramjetKafkaBackplaneTest {
  private static final boolean SETUP_TEARDOWN = true;
  private static final String BROKERS = get("flywheel.backplane.kafka.brokers", String::valueOf, "localhost:9092");
  private static final DockerCompose COMPOSE = new DockerCompose("flywheel", "stacks/kafka/docker-compose.yaml");
  private static final long START_DELAY_MILLIS = 5_000;
  
  @BeforeClass
  public static void beforeClass() throws Exception {
    if (SETUP_TEARDOWN) {
      DockerCompose.checkInstalled();
      TestSupport.logStatic("Starting Kafka stack... ");
      final long took = TestSupport.tookThrowing(() -> {
        COMPOSE.up();
        TestSupport.sleep(START_DELAY_MILLIS);
      });
      TestSupport.logStatic("took %,d ms\n", took);
    }
  }
  
  @AfterClass
  public static void afterClass() throws Exception {
    if (SETUP_TEARDOWN) {
      TestSupport.logStatic("Stopping Kafka stack... ");
      final long took = TestSupport.tookThrowing(() -> {
        COMPOSE.stop(1);
        COMPOSE.down(true);
      });
      TestSupport.logStatic("took %,d ms\n", took);
    }
  }
  
  @Override
  protected Kafka<String, KafkaData> getKafka() {
    return new KafkaCluster<>(new KafkaClusterConfig() {{
      common.with("bootstrap.servers", BROKERS);
    }});
  }
}
