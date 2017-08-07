package au.com.williamhill.flywheel.edge.auth;

public final class ProxyAuthResponse {
  private Long validMillis;

  public ProxyAuthResponse(Long validMillis) {
    this.validMillis = validMillis;
  }
  
  public Long getValidMillis() {
    return validMillis;
  }

  @Override
  public String toString() {
    return "ProxyAuthResponse [validMillis=" + validMillis + "]";
  }
}
