package au.com.williamhill.flywheel.edge.auth;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.auth.CachedAuthenticator.*;

public final class CachedAuthenticatorWrapper implements Authenticator<AuthConnector> {
  private final Authenticator<CachedAuthConnector> delegate;
  
  public CachedAuthenticatorWrapper(Authenticator<CachedAuthConnector> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void attach(AuthConnector connector) throws Exception {
    delegate.attach(new CachedAuthConnectorImpl(connector));
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
