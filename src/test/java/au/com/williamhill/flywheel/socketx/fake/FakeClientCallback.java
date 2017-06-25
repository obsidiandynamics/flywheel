package au.com.williamhill.flywheel.socketx.fake;

public interface FakeClientCallback {
  void connected();
  
  void disconnected();
  
  void received(int messages);
}
