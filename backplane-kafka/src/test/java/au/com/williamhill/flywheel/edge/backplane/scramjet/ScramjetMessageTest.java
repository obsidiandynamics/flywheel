package au.com.williamhill.flywheel.edge.backplane.scramjet;

import static junit.framework.TestCase.*;

import java.util.*;

import org.junit.*;

import com.google.gson.*;
import com.obsidiandynamics.indigo.util.*;
import com.obsidiandynamics.socketx.util.*;

public final class ScramjetMessageTest implements TestSupport {
  private Gson gson;
  
  @Before
  public void before() {
    gson = ScramjetMessage.defaultGsonBuilder().create();
  }
  
  private static ScramjetObject newTestObject() {
    return new ScramjetObject()
        .put("foo", Arrays.asList("a", "b"))
        .put("bar", Arrays.asList("c", "d"));
  }
  
  @Test
  public void testBase64Payload() {
    final byte[] bytes = BinaryUtils.toByteArray(0, 1, 2, 3, 4, 5, 6, 7);
    final ScramjetMessage d = test(new ScramjetMessage(UUID.randomUUID().toString(),
                                                       "TestMessage",
                                                       new ScramjetBase64(bytes),
                                                       "junit",
                                                       new Date()));
    final ScramjetBase64 base64 = (ScramjetBase64) d.getPayload();
    Assert.assertArrayEquals(bytes, base64.decode());
  }
  
  @Test
  public void testObjectPayload() {
    test(new ScramjetMessage(UUID.randomUUID().toString(),
                             "TestMessage",
                             newTestObject(),
                             "junit",
                             new Date()));
  }
  
  @Test
  public void testMapPayload() {
    test(new ScramjetMessage(UUID.randomUUID().toString(),
                             "TestMessage",
                             newTestObject().getAttributes(),
                             "junit",
                             new Date()));
  }
  
  @Test
  public void testStringPayload() {
    test(new ScramjetMessage(UUID.randomUUID().toString(),
                             "TestMessage",
                             "payload",
                             "junit",
                             new Date()));
  }
  
  @Test
  public void testDoublePayload() {
    test(new ScramjetMessage(UUID.randomUUID().toString(),
                             "TestMessage",
                             12.34,
                             "junit",
                             new Date()));
  }
  
  @Test
  public void testBooleanPayload() {
    test(new ScramjetMessage(UUID.randomUUID().toString(),
                             "TestMessage",
                             true,
                             "junit",
                             new Date()));
  }
  
  @Test
  public void testArrayPayload() {
    test(new ScramjetMessage(UUID.randomUUID().toString(),
                             "TestMessage",
                             Arrays.asList("eenie", "meenie", "minie"),
                             "junit",
                             new Date()));
  }
  
  @Test
  public void testNullPayload() {
    test(new ScramjetMessage(UUID.randomUUID().toString(),
                             "TestMessage",
                             null,
                             "junit",
                             new Date()));
  }
  
  @Test
  public void testNullFields() {
    test(new ScramjetMessage(null, null, null, null, null));
  }
  
  @Test
  public void testPushUpdateString() {
    final ScramjetPushUpdate push = new ScramjetPushUpdate("test/topic", 30, "testPayload");
    test(new ScramjetMessage(UUID.randomUUID().toString(),
                             "TestMessage",
                             push,
                             "junit",
                             new Date()));
  }
  
  @Test
  public void testPushUpdateMap() {
    final ScramjetPushUpdate push = new ScramjetPushUpdate("test/topic", 30, newTestObject().getAttributes());
    test(new ScramjetMessage(UUID.randomUUID().toString(),
                             "TestMessage",
                             push,
                             "junit",
                             new Date()));
  }
  
  private ScramjetMessage test(ScramjetMessage m) {
    final String json = m.toJson(gson);
    log("encoded=%s\n", json);
    final ScramjetMessage r = ScramjetMessage.fromJson(gson, json);
    log("decoded=%s\n", m);
    assertEquals(m.getId(), r.getId());
    assertEquals(m.getMessageType(), r.getMessageType());
    assertEquals(m.getPayload(), r.getPayload());
    assertEquals(m.getPublisher(), r.getPublisher());
    assertEquals(m.getSentAt(), r.getSentAt());
    assertEquals(m, r);
    assertEquals(m.hashCode(), r.hashCode());
    return r;
  }
}
