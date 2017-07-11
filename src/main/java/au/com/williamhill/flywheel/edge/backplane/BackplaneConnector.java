package au.com.williamhill.flywheel.edge.backplane;

public interface BackplaneConnector {
  void publish(String topic, String payload);
  
  void publish(String topic, byte[] payload);
}
