package au.com.williamhill.flywheel.topic;

public final class UnsubscribeResponse {
  private static final UnsubscribeResponse INSTANCE = new UnsubscribeResponse();
  
  static UnsubscribeResponse instance() { return INSTANCE; }
  
  UnsubscribeResponse() {}

  @Override
  public String toString() {
    return "UnsubscribeResponse";
  }
}
