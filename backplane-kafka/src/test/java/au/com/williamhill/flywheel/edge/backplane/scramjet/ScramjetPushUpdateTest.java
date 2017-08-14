package au.com.williamhill.flywheel.edge.backplane.scramjet;

import static org.junit.Assert.*;

import org.junit.*;

import com.google.gson.*;

public final class ScramjetPushUpdateTest {
  @Test
  public void test() {
    final String json = "{\"payload\":{\"topic\":\"racing/13800541/status\",\"payload\":\"{\\\"RaceId\\\":13800541,"
        + "\\\"RaceStatus\\\":\\\"Jumped\\\",\\\"JumpTime\\\":\\\"2017-08-13T05:26:39\\\","
        + "\\\"RaceDate\\\":\\\"2017-08-13T05:20:00Z\\\",\\\"ServerTime\\\":\\\"2017-08-13T05:26:40.101135Z\\\"}\","
        + "\"timeToLive\":30},\"messageType\":\"PUSH_UPDATE\",\"id\":\"2ca937a2-67a1-4032-891d-aaace62ebe57\""
        + ",\"sentAt\":\"2017-08-13T05:26:40.101135Z\",\"publisher\":\"RACING_API\"}";
    final Gson gson = ScramjetMessage.defaultGsonBuilder().create();
    final ScramjetMessage msg = ScramjetMessage.fromJson(gson, json);
    assertEquals("RACING_API", msg.getPublisher());
    assertEquals("PUSH_UPDATE", msg.getMessageType());
    
    //TODO uncomment when Gill pushes his update
    //final ScramjetPushUpdate update = (ScramjetPushUpdate) msg.getPayload();
    //assertEquals(30, update.getTimeToLive());
  }
}
