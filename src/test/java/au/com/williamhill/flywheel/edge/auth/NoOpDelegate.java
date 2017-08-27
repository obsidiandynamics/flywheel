package au.com.williamhill.flywheel.edge.auth;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;

@Y
public final class NoOpDelegate<C extends AuthConnector> implements Authenticator<C> {
  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {}
}
