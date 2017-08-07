package au.com.williamhill.flywheel.edge.auth;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;

@FunctionalInterface
public interface Authenticator extends AutoCloseable {
  public interface AuthenticationOutcome {
    void allow();
    
    void deny(TopicAccessError error);
    
    default void forbidden(String topic) {
      deny(new TopicAccessError("Forbidden", topic));
    }
  }
  
  default void init() throws Exception {};
  
  @Override
  default void close() throws Exception {};
  
  void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome);
}
