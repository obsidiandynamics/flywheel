package au.com.williamhill.flywheel.edge.backplane.kafka;

import static org.junit.Assert.*;

import java.io.*;
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
    serializer.configure(Collections.emptyMap(), false);
    deserializer = new ScramjetDeserializer();
    deserializer.configure(Collections.emptyMap(), false);
  }
  
  @After
  public void after() {
    serializer.close();
    deserializer.close();
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
  public void testNegativeTtl() {
    test(new KafkaData(UUID.randomUUID().toString(),
                       "testSource",
                       "testRoute",
                       null,
                       "textPayload",
                       30_000l,
                       20_000l));
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
    if (d.getExpiry() >= d.getTimestamp()) {
      assertEquals(d.getExpiry(), r.getExpiry());
    } else {
      assertEquals(d.getTimestamp(), r.getExpiry());
    }
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
  
  @Test
  public void testDeserializeStringPayload() throws IOException {
    final String json = getJson("string-payload.json");
    final KafkaData r = deserializer.deserialize("test", json.getBytes());
    assertEquals("race started", r.getTextPayload());
  }
  
  @Test
  public void testDeserializeJsonObjectPayload() throws IOException {
    final String json = getJson("json-object-payload.json");
    final KafkaData r = deserializer.deserialize("test", json.getBytes());
    assertEquals("{\"a\":\"b\"}", r.getTextPayload());
  }
  
  @Test
  public void testDeserializeJsonArrayPayload() throws IOException {
    final String json = getJson("json-array-payload.json");
    final KafkaData r = deserializer.deserialize("test", json.getBytes());
    assertEquals("[0.0,1.0,2.0]", r.getTextPayload());
  }
  
  @Test
  public void testDeserializeBase64Payload() throws IOException {
    final String json = getJson("base64-payload.json");
    final KafkaData r = deserializer.deserialize("test", json.getBytes());
    assertArrayEquals(BinaryUtils.toByteArray(0, 1, 2, 3, 4, 5, 6, 7), r.getBinaryPayload());
  }
  
  private static String getJson(String file) throws IOException {
    final InputStream in = ScramjetSerializerTest.class.getClassLoader().getResourceAsStream(file);
    final StringBuilder sb = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    }
    return sb.toString();
  }
}
