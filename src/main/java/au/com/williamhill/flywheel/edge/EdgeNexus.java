package au.com.williamhill.flywheel.edge;

import java.util.concurrent.*;

import au.com.williamhill.flywheel.*;
import au.com.williamhill.flywheel.frame.*;

public final class EdgeNexus implements AutoCloseable {
  private final EdgeNode node;
  
  private final Peer peer;
  
  private final Session session = new Session();

  public EdgeNexus(EdgeNode node, Peer peer) {
    this.node = node;
    this.peer = peer;
  }
  
  public Session getSession() {
    return session;
  }

  public CompletableFuture<SendOutcome> sendAuto(Frame frame) {
    return SendHelper.sendAuto(frame, peer.getEndpoint(), node.getWire());
  }
  
  public CompletableFuture<SendOutcome> send(TextEncodedFrame frame) {
    return SendHelper.send(frame, peer.getEndpoint(), node.getWire());
  }
  
  public void send(TextEncodedFrame frame, SendCallback callback) {
    SendHelper.send(frame, peer.getEndpoint(), node.getWire(), callback);
  }
  
  public CompletableFuture<SendOutcome> send(BinaryEncodedFrame frame) {
    return SendHelper.send(frame, peer.getEndpoint(), node.getWire());
  }
  
  public void send(BinaryEncodedFrame frame, SendCallback callback) {
    SendHelper.send(frame, peer.getEndpoint(), node.getWire(), callback);
  }
  
  public boolean isLocal() {
    return peer instanceof LocalPeer;
  }

  public Peer getPeer() {
    return peer;
  }

  @Override
  public void close() throws Exception {
    peer.close();
  }
  
  public boolean awaitClose(int waitMillis) throws InterruptedException {
    if (! peer.hasEndpoint()) throw new IllegalArgumentException("Cannot await close on a non-remote peer");
    return peer.getEndpoint().awaitClose(waitMillis);
  }

  @Override
  public String toString() {
    return "EdgeNexus [peer=" + peer + ", sessionId=" + (session != null ? session.getSessionId() : "<no session>") + "]";
  }
}
