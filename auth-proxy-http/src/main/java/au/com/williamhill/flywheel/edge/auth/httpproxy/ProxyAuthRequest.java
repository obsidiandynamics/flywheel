package au.com.williamhill.flywheel.edge.auth.httpproxy;

import au.com.williamhill.flywheel.frame.*;

public final class ProxyAuthRequest {
  private AuthCredentials credentials;
  
  private String topic;
  
  public ProxyAuthRequest(AuthCredentials credentials, String topic) {
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
    return "ProxyAuthRequest [credentials=" + credentials + ", topic=" + topic + "]";
  }
}
