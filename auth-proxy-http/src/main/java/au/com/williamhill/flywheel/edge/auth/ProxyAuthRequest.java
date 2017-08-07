package au.com.williamhill.flywheel.edge.auth;

import au.com.williamhill.flywheel.frame.*;

public final class ProxyAuthRequest {
  private Auth auth;
  
  private String topic;
  
  public ProxyAuthRequest(Auth auth, String topic) {
    this.auth = auth;
    this.topic = topic;
  }

  public Auth getAuth() {
    return auth;
  }

  public String getTopic() {
    return topic;
  }

  @Override
  public String toString() {
    return "ProxyAuthRequest [auth=" + auth + ", topic=" + topic + "]";
  }
}
