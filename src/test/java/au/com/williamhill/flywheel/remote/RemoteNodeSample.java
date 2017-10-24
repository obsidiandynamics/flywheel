package au.com.williamhill.flywheel.remote;

import java.net.*;

import com.obsidiandynamics.socketx.*;

import au.com.williamhill.flywheel.frame.*;

public final class RemoteNodeSample {
  public static void main(String[] args) throws Exception {
    final RemoteNode remote = RemoteNode
        .builder()
        .withClientConfig(new XClientConfig().withIdleTimeout(300_000))
        .build();

    final RemoteNexus nexus = remote.open(new URI("ws://localhost:8080/broker"), new RemoteNexusHandlerBase() {
      @Override public void onText(RemoteNexus nexus, String topic, String payload) {
        System.out.println("Message received: " + payload);
      }
    });

    final BindResponseFrame bindRes = nexus.bind(new BindFrame(null, null, null, new String[] { "quotes/#" }, null, null)).get();
    System.out.println("Bind response " + bindRes);
    
    nexus.publish(new PublishTextFrame("quotes/AAPL", "160.82"));
  }
}
