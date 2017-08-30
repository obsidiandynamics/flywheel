package au.com.williamhill.flywheel.socketx;

import java.net.*;
import java.security.*;

import javax.net.ssl.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.socketx.ssl.*;
import au.com.williamhill.flywheel.socketx.util.*;

@Y
public class SSLConfig {
  @YInject
  private String keyStoreLocation;
  
  @YInject
  private String keyStorePassword;
  
  @YInject
  private String trustStoreLocation;
  
  @YInject
  private String trustStorePassword;
  
  @YInject
  private String keyPassword;

  public SSLConfig withKeyStoreLocation(String keyStoreLocation) {
    this.keyStoreLocation = keyStoreLocation;
    return this;
  }
  
  public SSLConfig withKeyStorePassword(String keyStorePassword) {
    this.keyStorePassword = keyStorePassword;
    return this;
  }

  public SSLConfig withTrustStoreLocation(String trustStoreLocation) {
    this.trustStoreLocation = trustStoreLocation;
    return this;
  }
  
  public SSLConfig withTrustStorePassword(String keyStorePassword) {
    this.keyStorePassword = keyStorePassword;
    return this;
  }

  public SSLConfig withKeyPassword(String keyPassword) {
    this.keyPassword = keyPassword;
    return this;
  }
  
  final SSLContext buildSSLContext() throws Exception {
    final KeyStore keyStore = JKS.loadKeyStore(ResourceLocator.asStream(new URI(keyStoreLocation)), keyStorePassword);
    final KeyStore trustStore = JKS.loadKeyStore(ResourceLocator.asStream(new URI(trustStoreLocation)), trustStorePassword);
    return JKS.createSSLContext(keyStore, keyPassword, trustStore);
  }
  
  public static SSLConfig getDefault() {
    return new SSLConfig()
        .withKeyStoreLocation("cp://keystore.jks")
        .withKeyStorePassword("storepass")
        .withTrustStoreLocation("cp://keystore.jks")
        .withTrustStorePassword("storepass")
        .withKeyPassword("keypass");
  }

  @Override
  public String toString() {
    return "SSLConfig [keyStoreLocation: " + keyStoreLocation + ", trustStoreLocation: " + trustStoreLocation + "]";
  }
}
