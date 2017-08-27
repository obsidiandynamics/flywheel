package au.com.williamhill.flywheel.edge.auth;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;

@FunctionalInterface
public interface Authenticator<C extends AuthConnector> extends AutoCloseable {
  public interface AuthenticationOutcome {
    long INDEFINITE = 0;
    
    void allow(long millis);
    
    void deny(TopicAccessError error);
    
    default void forbidden(String topic) {
      deny(new TopicAccessError("Forbidden", topic));
    }
  }
  
  default void attach(C connector) throws Exception {}
  
  @Override
  default void close() throws Exception {}
  
  void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome);
}
