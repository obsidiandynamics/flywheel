package au.com.williamhill.flywheel.edge;

import java.util.*;

import au.com.williamhill.flywheel.frame.*;

public final class Session {
  private final long connectTime = System.currentTimeMillis();
  
  private volatile String sessionId;
  
  private volatile AuthCredentials credentials;
  
  private volatile Subscription subscription = () -> Collections.emptySet();
  
  Session() {}
  
  public long getConnectTime() {
    return connectTime;
  }
  
  public boolean hasSessionId() {
    return sessionId != null;
  }

  public String getSessionId() {
    return sessionId;
  }
  
  void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }
  
  @SuppressWarnings("unchecked")
  public <C extends AuthCredentials> C getCredentials() {
    return (C) credentials;
  }
  
  public void setCredentials(AuthCredentials credentials) {
    this.credentials = credentials;
  }
  
  @SuppressWarnings("unchecked")
  <S extends Subscription> S getSubscription() {
    return (S) subscription;
  }

  void setSubscription(Subscription subscription) {
    this.subscription = subscription;
  }
}
