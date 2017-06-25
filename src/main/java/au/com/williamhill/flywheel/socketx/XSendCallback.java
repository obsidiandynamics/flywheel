package au.com.williamhill.flywheel.socketx;

public interface XSendCallback {
  void onComplete(XEndpoint endpoint);

  void onError(XEndpoint endpoint, Throwable cause);
}
