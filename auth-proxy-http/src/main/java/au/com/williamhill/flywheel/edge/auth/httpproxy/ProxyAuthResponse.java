package au.com.williamhill.flywheel.edge.auth.httpproxy;

public final class ProxyAuthResponse {
  private Long allowMillis;

  public ProxyAuthResponse(Long allowMillis) {
    this.allowMillis = allowMillis;
  }
  
  public Long getAllowMillis() {
    return allowMillis;
  }

  @Override
  public String toString() {
    return "ProxyAuthResponse [allowMillis=" + allowMillis + "]";
  }
}
