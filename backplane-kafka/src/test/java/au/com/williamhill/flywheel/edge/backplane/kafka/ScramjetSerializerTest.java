package au.com.williamhill.flywheel.edge.backplane.kafka;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.kafka.common.serialization.*;
import org.junit.*;

import com.google.gson.*;
import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.util.*;

public final class ScramjetSerializerTest implements TestSupport {
  private Serializer<KafkaData> serializer;
  
  private Deserializer<KafkaData> deserializer;
  
  @Before
  public void before() {
    serializer = new ScramjetSerializer();
    deserializer = new ScramjetDeserializer();
  }
  
  @Test
  public void testText() {
    test(new KafkaData(UUID.randomUUID().toString(),
                       "testSource",
                       "testRoute",
                       null,
                       "textPayload",
                       30_000l,
                       65_000l));
  }
  
  @Test
  public void testBinary() {
    test(new KafkaData(UUID.randomUUID().toString(),
                       "testSource",
                       "testRoute",
                       BinaryUtils.toByteArray(0, 1, 2, 3, 4, 5, 6, 7),
                       null,
                       30_000l,
                       65_000l));
  }

  private void test(KafkaData d) {
    final byte[] bytes = serializer.serialize("test", d);
    log("encoded:\n%s\n", BinaryUtils.dump(bytes));
    final KafkaData r = deserializer.deserialize("test", bytes);
    log("decoded: %s\n", r);
    assertEquals(d.getId(), r.getId());
    assertEquals(d.getSource(), r.getSource());
    assertEquals(d.getTopic(), r.getTopic());
    assertArrayEquals(d.getBinaryPayload(), r.getBinaryPayload());
    assertEquals(d.getTextPayload(), r.getTextPayload());
    assertEquals(d.getTimestamp(), r.getTimestamp());
    assertEquals(d.getExpiry(), r.getExpiry());
  }
  
  @Test
  public void testDeserializeError() {
    final KafkaData d = deserializer.deserialize("test", "nonsense".getBytes());
    assertTrue(d.isError());
    assertEquals(JsonSyntaxException.class, d.getError().getClass());
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testSerializeError() {
    final KafkaData d = new KafkaData(new RuntimeException());
    serializer.serialize("test", d);
  }
}
