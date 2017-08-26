package au.com.williamhill.flywheel.edge.auth;

import au.com.williamhill.flywheel.edge.*;

public final class AuthenticatorWrapper implements Authenticator {
  private final NestedAuthenticator delegate;
  
  public AuthenticatorWrapper(NestedAuthenticator delegate) {
    this.delegate = delegate;
  }

  @Override
  public void attach(AuthConnector connector) throws Exception {
    delegate.attach(connector);
  }
  
  @Override
  public void close() throws Exception {
    delegate.close();
  }

  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
    delegate.verify(nexus, topic, outcome);
  }
}
