package au.com.williamhill.flywheel.edge;

import au.com.williamhill.flywheel.frame.*;

public abstract class TopicListenerBase implements TopicListener {
  @Override public void onOpen(EdgeNexus nexus) {}
  
  @Override public void onClose(EdgeNexus nexus) {}
  
  @Override public void onBind(EdgeNexus nexus, BindFrame bind, BindResponseFrame bindRes) {}
  
  @Override public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {}
  
  @Override public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {}
}
