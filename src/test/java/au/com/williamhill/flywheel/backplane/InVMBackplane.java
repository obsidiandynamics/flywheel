package au.com.williamhill.flywheel.backplane;

import java.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.frame.*;

public final class InVMBackplane implements Backplane {
  private final List<EdgeNode> edges = new ArrayList<>();

  @Override
  public void attach(EdgeNode edge) {
    edges.add(edge);
    edge.addTopicListener(new TopicListenerBase() {
      @Override public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {
        if (nexus.isLocal() || edges.size() == 1) return;
        
        for (EdgeNode e : edges) {
          if (e != edge) {
            e.publish(pub.getTopic(), pub.getPayload());
          }
        }
      }
      
      @Override public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
        if (nexus.isLocal() || edges.size() == 1) return;
        
        for (EdgeNode e : edges) {
          if (e != edge) {
            e.publish(pub.getTopic(), pub.getPayload());
          }
        }
      }
    });
  }

  @Override
  public void close() throws Exception {
    edges.clear();
  }
}
