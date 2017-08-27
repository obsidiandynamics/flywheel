package au.com.williamhill.flywheel.edge.auth;

import au.com.williamhill.flywheel.edge.*;

class MockAuthenticator implements NestedAuthenticator {
  private volatile long allowMillis;
  
  MockAuthenticator(long allowMillis) {
    this.allowMillis = allowMillis;
  }
  
  void set(long allowMillis) {
    this.allowMillis = allowMillis;
  }

  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
    if (allowMillis >= 0) outcome.allow(allowMillis);
    else outcome.forbidden(topic);
  }
  
  @Override public void attach(AuthConnector connector) {}
  
  @Override public void close() {}
}