package au.com.williamhill.flywheel.edge.auth.httpstub;

import au.com.williamhill.flywheel.frame.*;

public final class StubAuthRequest {
  private AuthCredentials credentials;
  
  private String topic;
  
  public StubAuthRequest(AuthCredentials credentials, String topic) {
    this.credentials = credentials;
    this.topic = topic;
  }

  public AuthCredentials getCredentials() {
    return credentials;
  }

  public String getTopic() {
    return topic;
  }

  @Override
  public String toString() {
    return "StubAuthRequest [credentials=" + credentials + ", topic=" + topic + "]";
  }
}
