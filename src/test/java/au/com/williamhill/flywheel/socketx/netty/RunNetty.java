package au.com.williamhill.flywheel.socketx.netty;

import au.com.williamhill.flywheel.socketx.*;

public final class RunNetty {
  public static void main(String[] args) throws Exception {
    System.setProperty("io.netty.noUnsafe", Boolean.toString(true));
    final XServer<NettyEndpoint> netty = NettyServer.factory().create(new XServerConfig() {{
      port = 6667;
      contextPath = "/";
    }}, null);
    netty.close();
  }
}
