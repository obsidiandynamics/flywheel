package au.com.williamhill.flywheel.edge.auth;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;

@Y(DenyAllAuthenticator.Mapper.class)
public final class DenyAllAuthenticator implements Authenticator<AuthConnector> {
  public static final class Mapper implements TypeMapper {
    @Override public Object map(YObject y, Class<?> type) {
      return instance();
    }
  }
  
  private static final DenyAllAuthenticator INSTANCE = new DenyAllAuthenticator();
  
  private DenyAllAuthenticator() {}
  
  public static DenyAllAuthenticator instance() { return INSTANCE; }
  
  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
    outcome.forbidden(topic);
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
