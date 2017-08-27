package au.com.williamhill.flywheel.edge.auth.httpstub;

public final class StubAuthResponse {
  private Long allowMillis;

  public StubAuthResponse(Long allowMillis) {
    this.allowMillis = allowMillis;
  }
  
  public Long getAllowMillis() {
    return allowMillis;
  }
  
  public boolean isAllow() {
    return allowMillis != null;
  }

  @Override
  public String toString() {
    return "StubAuthResponse [allowMillis=" + allowMillis + "]";
  }
}
