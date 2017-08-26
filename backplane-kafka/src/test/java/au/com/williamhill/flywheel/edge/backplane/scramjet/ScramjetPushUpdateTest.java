package au.com.williamhill.flywheel.edge.backplane.scramjet;

import static org.junit.Assert.*;

import org.junit.*;

import com.google.gson.*;

public final class ScramjetPushUpdateTest {
  @Test
  public void test() {
    final String json = "{\"payload\":{\"$type\":\"Scramjet.Messages.Push.Update\",\"topic\":\"racing/13833998/status\","
        + "\"payload\":\"{\\\"RaceId\\\":13833998,\\\"RaceStatus\\\":\\\"Jumped\\\",\\\"JumpTime\\\":\\\"2017-08-22T16:03:28\\\","
        + "\\\"RaceDate\\\":\\\"2017-08-20T06:25:00Z\\\",\\\"ServerTime\\\":\\\"2017-08-22T16:03:30.965908Z\\\"}\","
        + "\"timeToLive\":30},\"messageType\":\"PUSH_UPDATE\",\"id\":\"e5e58191-1609-40e7-82a5-c8b4733ca333\","
        + "\"sentAt\":\"2017-08-22T16:03:30.965908Z\",\"publisher\":\"RACING_API\"}";
    final Gson gson = ScramjetMessage.defaultGsonBuilder().create();
    final ScramjetMessage msg = ScramjetMessage.fromJson(gson, json);
    assertEquals("RACING_API", msg.getPublisher());
    assertEquals("PUSH_UPDATE", msg.getMessageType());
    
    assertEquals("RACING_API", msg.getPublisher());
    assertEquals("PUSH_UPDATE", msg.getMessageType());
    assertEquals("e5e58191-1609-40e7-82a5-c8b4733ca333", msg.getId());
    final ScramjetPushUpdate update = (ScramjetPushUpdate) msg.getPayload();
    assertEquals(30, update.getTimeToLive());
    assertEquals("racing/13833998/status", update.getTopic());
  }
}
