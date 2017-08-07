package au.com.williamhill.flywheel.edge.auth;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;

@Y(AllowAllAuth.Mapper.class)
public final class AllowAllAuth implements Authenticator {
  public static final class Mapper implements TypeMapper {
    @Override public Object map(YObject y, Class<?> type) {
      return INSTANCE;
    }
  }
  
  private static final AllowAllAuth INSTANCE = new AllowAllAuth();
  
  private AllowAllAuth() {}
  
  public static AllowAllAuth instance() { return INSTANCE; }
  
  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
    outcome.allow();
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
