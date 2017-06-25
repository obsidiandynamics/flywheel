package au.com.williamhill.flywheel.edge.auth;

import au.com.williamhill.flywheel.*;
import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;

public final class RemoteTopicAuthenticator implements Authenticator {
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
}
