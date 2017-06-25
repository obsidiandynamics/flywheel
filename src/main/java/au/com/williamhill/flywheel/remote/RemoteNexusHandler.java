package au.com.williamhill.flywheel.remote;

import java.nio.*;

public interface RemoteNexusHandler {
  void onOpen(RemoteNexus nexus);
  
  void onClose(RemoteNexus nexus);
  
  void onText(RemoteNexus nexus, String topic, String payload);
  
  void onBinary(RemoteNexus nexus, String topic, ByteBuffer payload);
}
