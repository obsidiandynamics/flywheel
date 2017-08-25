package au.com.williamhill.flywheel.edge.auth;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.*;

@Y(SubAuthChain.Mapper.class)
public final class SubAuthChain extends AuthChain<SubAuthChain> {
  public static final class Mapper extends AuthChainMapper<SubAuthChain> {
    @Override AuthChain<SubAuthChain> getBaseChain() {
      return new SubAuthChain();
    }
  }
  
  public SubAuthChain() {
    registerDefaults();
  }
  
  private SubAuthChain registerDefaults() {
    set("", AllowAllAuthenticator.instance());
    set(Flywheel.REMOTE_PREFIX, RemoteTopicAuthenticator.instance());
    return this;
  }
}
