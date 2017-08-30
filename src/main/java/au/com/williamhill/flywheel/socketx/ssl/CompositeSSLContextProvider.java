package au.com.williamhill.flywheel.socketx.ssl;

import javax.net.ssl.*;

public class CompositeSSLContextProvider implements SSLContextProvider {
  KeyManagerProvider keyManagerProvider;
  
  TrustManagerProvider trustManagerProvider;
  
  public final CompositeSSLContextProvider withKeyManagerProvider(KeyManagerProvider keyManagerProvider) {
    this.keyManagerProvider = keyManagerProvider;
    return this;
  }

  public final CompositeSSLContextProvider withTrustManagerProvider(TrustManagerProvider trustManagerProvider) {
    this.trustManagerProvider = trustManagerProvider;
    return this;
  }

  @Override
  public final SSLContext getSSLContext() throws Exception {
    final SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(keyManagerProvider.getKeyManagers(), 
                    trustManagerProvider.getTrustManagers(),
                    null);
    return sslContext;
  }

  @Override
  public final String toString() {
    return "CompositeSSLContextProvider [keyManagerProvider: " + keyManagerProvider + ", trustManagerProvider: "
           + trustManagerProvider + "]";
  }
}
