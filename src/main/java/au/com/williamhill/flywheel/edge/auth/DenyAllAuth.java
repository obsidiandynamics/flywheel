package au.com.williamhill.flywheel.edge.auth;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;

@Y(DenyAllAuth.Mapper.class)
public final class DenyAllAuth implements Authenticator {
  public static final class Mapper implements TypeMapper {
    @Override public Object map(YObject y, Class<?> type) {
      return INSTANCE;
    }
  }
  
  private static final DenyAllAuth INSTANCE = new DenyAllAuth();
  
  private DenyAllAuth() {}
  
  public static DenyAllAuth instance() { return INSTANCE; }
  
  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
    outcome.forbidden(topic);
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
