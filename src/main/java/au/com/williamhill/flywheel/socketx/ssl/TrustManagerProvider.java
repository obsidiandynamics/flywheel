package au.com.williamhill.flywheel.socketx.ssl;

import javax.net.ssl.*;

public interface TrustManagerProvider {
  TrustManager[] getTrustManagers() throws Exception;
}
