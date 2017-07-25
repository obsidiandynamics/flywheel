package au.com.williamhill.flywheel.rig;

final class Announce extends RigSubframe {
  enum Role {
    CONTROL, SUBSCRIBER
  }
  
  private final Role role;
  private final String controlSessionId;
  
  Announce(Role role, String controlSessionId) {
    this.role = role;
    this.controlSessionId = controlSessionId;
  }
  
  Role getRole() {
    return role;
  }
  
  String getControlSessionId() {
    return controlSessionId;
  }

  @Override
  public String toString() {
    return "Announce [role=" + role + ", controlSessionId=" + controlSessionId + "]";
  }
}
