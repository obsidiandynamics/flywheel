package au.com.williamhill.flywheel.socketx.ssl;

import javax.net.ssl.*;

public interface SSLContextProvider {
  SSLContext getSSLContext() throws Exception;
}
