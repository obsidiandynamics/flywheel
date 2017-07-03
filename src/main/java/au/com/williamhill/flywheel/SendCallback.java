package au.com.williamhill.flywheel;

@FunctionalInterface
public interface SendCallback {
  void onCallback(SendOutcome outcome, Throwable cause);
}
