package au.com.williamhill.flywheel.edge.backplane.kafka;

import org.junit.*;

public final class KafkaDataTest {
  @Test(expected=IllegalArgumentException.class)
  public void testConstructorValidation() {
    new KafkaData(null, null, null, null, null, 0, 0);
  }
}
