package au.com.williamhill.flywheel.edge.auth;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;

@Y
public final class NoOpDelegate implements NestedAuthenticator {
  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {}
}
