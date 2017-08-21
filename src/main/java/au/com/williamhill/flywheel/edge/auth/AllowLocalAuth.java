package au.com.williamhill.flywheel.edge.auth;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;

@Y(AllowLocalAuth.Mapper.class)
public final class AllowLocalAuth implements Authenticator {
  public static final class Mapper implements TypeMapper {
    @Override public Object map(YObject y, Class<?> type) {
      return INSTANCE;
    }
  }
  
  private static final AllowLocalAuth INSTANCE = new AllowLocalAuth();
  
  private AllowLocalAuth() {}
  
  public static AllowLocalAuth instance() { return INSTANCE; }
  
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
