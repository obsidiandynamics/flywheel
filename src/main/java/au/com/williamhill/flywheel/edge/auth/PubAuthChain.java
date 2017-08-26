package au.com.williamhill.flywheel.edge.auth;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.*;

@Y(PubAuthChain.Mapper.class)
public final class PubAuthChain extends AuthChain<PubAuthChain> {
  public static final class Mapper extends AuthChainMapper<PubAuthChain> {
    @Override AuthChain<PubAuthChain> getBaseChain() {
      return new PubAuthChain();
    }
  }
  
  public PubAuthChain() {
    registerDefaults();
  }
  
  private PubAuthChain registerDefaults() {
    set("", AllowAllAuthenticator.instance());
    set(Flywheel.REMOTE_PREFIX, RemoteTopicAuthenticator.instance());
    return this;
  }
}
