package au.com.williamhill.flywheel.edge.auth.httpproxy;

public final class ProxyAuthResponse {
  private long allowMillis;

  public ProxyAuthResponse(long allowMillis) {
    this.allowMillis = allowMillis;
  }
  
  public long getAllowMillis() {
    return allowMillis;
  }

  @Override
  public String toString() {
    return "ProxyAuthResponse [allowMillis=" + allowMillis + "]";
  }
}
