package au.com.williamhill.flywheel.edge.auth;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;

@Y(AllowAllAuthenticator.Mapper.class)
public final class AllowAllAuthenticator implements Authenticator {
  public static final class Mapper implements TypeMapper {
    @Override public Object map(YObject y, Class<?> type) {
      return instance();
    }
  }
  
  private static final AllowAllAuthenticator INSTANCE = new AllowAllAuthenticator();
  
  private AllowAllAuthenticator() {}
  
  public static AllowAllAuthenticator instance() { return INSTANCE; }
  
  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
    outcome.allow(AuthenticationOutcome.INDEFINITE);
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
