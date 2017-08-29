package au.com.williamhill.flywheel.socketx.util;

import java.io.*;
import java.security.*;

import javax.net.ssl.*;

public final class SSL {
  private SSL() {}

  public static SSLContext createSSLContext(KeyStore keyStore, String keyPassword, KeyStore trustStore) throws Exception {
    final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(keyStore, keyPassword.toCharArray());
    final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
    
    final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(trustStore);
    final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

    final SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(keyManagers, trustManagers, null);

    return sslContext;
  }

  public static KeyStore loadKeyStore(InputStream stream, String storePassword) throws Exception {
    try (InputStream is = stream) {
      final KeyStore loadedKeystore = KeyStore.getInstance("JKS");
      loadedKeystore.load(is, storePassword.toCharArray());
      return loadedKeystore;
    }
  }
}
