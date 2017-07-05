package au.com.williamhill.flywheel.backplane;

import java.nio.*;
import java.util.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.backplane.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.util.*;

public final class InVMBackplane implements Backplane {
  private final List<EdgeNode> edges = new ArrayList<>();

  @Override
  public void attach(EdgeNode edge) {
    edges.add(edge);
    edge.addTopicListener(new TopicListenerBase() {
      @Override public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {
        if (nexus.isLocal()) return;
        
        for (EdgeNode e : edges) {
          if (e != edge) {
            e.publish(pub.getTopic(), pub.getPayload());
          }
        }
      }
      
      @Override public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
        if (nexus.isLocal()) return;
        
        final byte[] bytes = BinaryUtils.toByteArray(pub.getPayload());
        for (EdgeNode e : edges) {
          if (e != edge) {
            e.publish(pub.getTopic(), ByteBuffer.wrap(bytes));
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
