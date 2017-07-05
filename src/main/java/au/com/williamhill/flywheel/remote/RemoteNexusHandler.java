package au.com.williamhill.flywheel.remote;

public interface RemoteNexusHandler {
  void onOpen(RemoteNexus nexus);
  
  void onClose(RemoteNexus nexus);
  
  void onText(RemoteNexus nexus, String topic, String payload);
  
  void onBinary(RemoteNexus nexus, String topic, byte[] payload);
}
