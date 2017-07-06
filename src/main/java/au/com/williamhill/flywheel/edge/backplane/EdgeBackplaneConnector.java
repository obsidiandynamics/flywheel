package au.com.williamhill.flywheel.edge.backplane;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;

public final class EdgeBackplaneConnector implements BackplaneConnector {
  private final EdgeNode edge;
  
  public EdgeBackplaneConnector(EdgeNode edge, Backplane backplane) {
    this.edge = edge;
    edge.addTopicListener(new TopicListenerBase() {
      @Override public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {
        backplane.onPublish(EdgeBackplaneConnector.this, nexus, pub);
      }
      
      @Override public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
        backplane.onPublish(EdgeBackplaneConnector.this, nexus, pub);
      }
    });
  }

  @Override
  public void publish(String topic, String payload) {
    edge.publish(topic, payload);
  }

  @Override
  public void publish(String topic, byte[] payload) {
    edge.publish(topic, payload);
  }
}
