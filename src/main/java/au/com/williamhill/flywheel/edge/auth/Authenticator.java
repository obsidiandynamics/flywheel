package au.com.williamhill.flywheel.edge.auth;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;

@FunctionalInterface
public interface Authenticator {
  public interface AuthenticationOutcome {
    void allow();
    
    void deny(TopicAccessError error);
    
    default void forbidden(String topic) {
      deny(new TopicAccessError("Forbidden", topic));
    }
  }
  
  void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome);
}
