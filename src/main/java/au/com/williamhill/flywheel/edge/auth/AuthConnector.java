package au.com.williamhill.flywheel.edge.auth;

import java.util.*;

import au.com.williamhill.flywheel.edge.*;

public interface AuthConnector {
  Collection<String> getLiveTopics(EdgeNexus nexus);
  
  void expireTopic(EdgeNexus nexus, String topic);
}
