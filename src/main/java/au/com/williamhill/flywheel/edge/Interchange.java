package au.com.williamhill.flywheel.edge;

import java.util.*;
import java.util.concurrent.*;

import au.com.williamhill.flywheel.frame.*;

public interface Interchange extends AutoCloseable {
  void onOpen(EdgeNexus nexus);
  
  void onClose(EdgeNexus nexus);
  
  CompletableFuture<Void> onBind(EdgeNexus nexus, Set<String> subscribe, Set<String> unsubscribe);
  
  void onPublish(EdgeNexus nexus, PublishTextFrame pub);
  
  void onPublish(EdgeNexus nexus, PublishBinaryFrame pub);
}
