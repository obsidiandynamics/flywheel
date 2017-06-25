package au.com.williamhill.flywheel.rig;

final class Wait extends RigSubframe {
  private final long expectedMessages;
  
  Wait(long expectedMessages) {
    this.expectedMessages = expectedMessages;
  }

  final long getExpectedMessages() {
    return expectedMessages;
  }

  @Override
  public String toString() {
    return "Wait [expectedMessages=" + expectedMessages + "]";
  }
}
