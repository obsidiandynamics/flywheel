package au.com.williamhill.flywheel.backplane.scramjet;

import static junit.framework.TestCase.*;

import java.util.*;

import org.junit.*;

import com.google.gson.*;
import com.obsidiandynamics.indigo.util.*;

public final class ScramjetMessageTest implements TestSupport {
  private Gson gson;
  
  @Before
  public void before() {
    gson = ScramjetMessage.defaultGsonBuilder().create();
  }
  
  @Test
  public void testObjectPayload() {
    final ScramjetObject obj = new ScramjetObject();
    obj.atts.put("foo", Arrays.asList("a", "b"));
    obj.atts.put("bar", Arrays.asList("c", "d"));
    test(new ScramjetMessage(UUID.randomUUID().toString(),
                             "TestMessage",
                             obj,
                             "junit",
                             new Date()));
  }
  
  @Test
  public void testPushUpdate() {
    final ScramjetPushUpdate push = new ScramjetPushUpdate("test/topic", 30, "testPayload");
    test(new ScramjetMessage(UUID.randomUUID().toString(),
                             "TestMessage",
                             push,
                             "junit",
                             new Date()));
  }
  
//  @Test
//  public void testStringPayload() {
//    test(new ScramjetMessage(UUID.randomUUID().toString(),
//                             "TestMessage",
//                             "payload",
//                             "junit",
//                             new Date()));
//  }
  
  private void test(ScramjetMessage m) {
    final String json = m.toJson(gson);
    log("encoded=%s\n", json);
    final ScramjetMessage r = ScramjetMessage.fromJson(gson, json);
    log("decoded=%s\n", m);
    assertEquals(m.getId(), r.getId());
    assertEquals(m.getMessageType(), r.getMessageType());
    assertEquals(m.getPayload(), r.getPayload());
    assertEquals(m.getPublisher(), r.getPublisher());
    assertEquals(m.getSentAt(), r.getSentAt());
  }
}
