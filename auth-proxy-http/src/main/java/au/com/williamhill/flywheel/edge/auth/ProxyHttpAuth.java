package au.com.williamhill.flywheel.edge.auth;

import java.io.*;
import java.net.*;
import java.security.*;

import javax.net.ssl.*;

import org.apache.http.config.*;
import org.apache.http.impl.nio.client.*;
import org.apache.http.impl.nio.conn.*;
import org.apache.http.impl.nio.reactor.*;
import org.apache.http.nio.conn.*;
import org.apache.http.nio.conn.ssl.*;
import org.apache.http.nio.reactor.*;
import org.apache.http.ssl.*;
import org.slf4j.*;

import au.com.williamhill.flywheel.edge.*;

public final class ProxyHttpAuth implements Authenticator {
  private static final Logger LOG = LoggerFactory.getLogger(ProxyHttpAuth.class);

  private URI uri;

  private int poolSize = 8;

  private CloseableHttpAsyncClient httpClient;

  public ProxyHttpAuth withUri(URI uri) {
    this.uri = uri;
    return this;
  }

  public ProxyHttpAuth withPoolSize(int poolSize) {
    this.poolSize = poolSize;
    return this;
  }

  @Override
  public void init() throws IOReactorException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
    final HostnameVerifier hostnameVerifier = (s, sslSession) -> true;
    final Registry<SchemeIOSessionStrategy> sslSessionStrategy = RegistryBuilder
        .<SchemeIOSessionStrategy>create()
        .register("http", NoopIOSessionStrategy.INSTANCE)
        .register("https", new SSLIOSessionStrategy(getSSLContext(), hostnameVerifier)).build();

    final ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
    final PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor, sslSessionStrategy);
    cm.setMaxTotal(poolSize);
    cm.setDefaultMaxPerRoute(poolSize);

    httpClient = HttpAsyncClients.custom().setConnectionManager(cm).build();
    httpClient.start();
  }

  private static SSLContext getSSLContext() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
    return SSLContexts.custom().loadTrustMaterial(null, (certificate, authType) -> true).build();
  }

  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
    // TODO Auto-generated method stub

  }
  
  @Override
  public void close() throws IOException {
    httpClient.close();
  }
}
