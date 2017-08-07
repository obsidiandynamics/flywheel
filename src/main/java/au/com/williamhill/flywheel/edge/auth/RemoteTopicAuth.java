package au.com.williamhill.flywheel.edge.auth;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.*;
import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;

@Y(RemoteTopicAuth.Mapper.class)
public final class RemoteTopicAuth implements Authenticator {
  public static final class Mapper implements TypeMapper {
    @Override public Object map(YObject y, Class<?> type) {
      return INSTANCE;
    }
  }
  
  private static final RemoteTopicAuth INSTANCE = new RemoteTopicAuth();
  
  private RemoteTopicAuth() {}
  
  public static RemoteTopicAuth instance() { return INSTANCE; }
  
  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
    final String sessionId = nexus.getSession().getSessionId();
    if (sessionId == null) {
      outcome.deny(new TopicAccessError("No session ID", topic));
      return;
    }
    
    final String allowedTopicPrefix = Flywheel.getSessionTopicPrefix(sessionId);
    if (topic.startsWith(allowedTopicPrefix)) {
      outcome.allow();
    } else{
      outcome.deny(new TopicAccessError(String.format("Restricted to %s/#", allowedTopicPrefix), topic));
    }
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
