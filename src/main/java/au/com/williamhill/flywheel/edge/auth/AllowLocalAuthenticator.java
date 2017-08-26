package au.com.williamhill.flywheel.edge.auth;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;

@Y(AllowLocalAuthenticator.Mapper.class)
public final class AllowLocalAuthenticator implements Authenticator {
  public static final class Mapper implements TypeMapper {
    @Override public Object map(YObject y, Class<?> type) {
      return instance();
    }
  }
  
  private static final AllowLocalAuthenticator INSTANCE = new AllowLocalAuthenticator();
  
  private AllowLocalAuthenticator() {}
  
  public static AllowLocalAuthenticator instance() { return INSTANCE; }
  
  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
    if (nexus.isLocal()) {
      outcome.allow(AuthenticationOutcome.INDEFINITE);
    } else {
      outcome.forbidden(topic);
    }
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
