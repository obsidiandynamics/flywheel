package au.com.williamhill.flywheel;

import java.nio.*;
import java.util.concurrent.*;

import com.obsidiandynamics.socketx.*;

import au.com.williamhill.flywheel.frame.*;

public final class SendHelper {
  private SendHelper() {}

  public static CompletableFuture<SendOutcome> sendAuto(Frame frame, XEndpoint endpoint, Wire wire) {
    if (frame instanceof TextFrame) {
      return send((TextFrame) frame, endpoint, wire);
    } else {
      return send((BinaryFrame) frame, endpoint, wire);
    }
  }
  
  public static void sendAuto(Frame frame, XEndpoint endpoint, Wire wire, SendCallback callback) {
    if (frame instanceof TextFrame) {
      send((TextFrame) frame, endpoint, wire, callback);
    } else {
      send((BinaryFrame) frame, endpoint, wire, callback);
    }
  }

  public static CompletableFuture<SendOutcome> send(TextEncodedFrame frame, XEndpoint endpoint, Wire wire) {
    final CompletableFuture<SendOutcome> f = new CompletableFuture<>();
    final String encoded = wire.encode(frame);
    endpoint.send(encoded, wrapFuture(f));
    return f;
  }
  
  public static void send(TextEncodedFrame frame, XEndpoint endpoint, Wire wire, SendCallback callback) {
    final String encoded = wire.encode(frame);
    endpoint.send(encoded, wrapCallback(callback));
  }

  public static CompletableFuture<SendOutcome> send(BinaryEncodedFrame frame, XEndpoint endpoint, Wire wire) {
    final CompletableFuture<SendOutcome> f = new CompletableFuture<>();
    final ByteBuffer encoded = wire.encode(frame);
    endpoint.send(encoded, wrapFuture(f));
    return f;
  }
  
  public static void send(BinaryEncodedFrame frame, XEndpoint endpoint, Wire wire, SendCallback callback) {
    final ByteBuffer encoded = wire.encode(frame);
    endpoint.send(encoded, wrapCallback(callback));
  }
  
  static XSendCallback wrapFuture(CompletableFuture<SendOutcome> f) {
    return new XSendCallback() {
      @Override public void onComplete(XEndpoint endpoint) {
        f.complete(SendOutcome.SENT);
      }

      @Override public void onError(XEndpoint endpoint, Throwable cause) {
        f.completeExceptionally(cause);
      }

      @Override public void onSkip(XEndpoint endpoint) {
        f.complete(SendOutcome.SKIPPED);
      }
    };
  }
  
  static XSendCallback wrapCallback(SendCallback callback) {
    if (callback != null) {
      return new XSendCallback() {
        @Override public void onComplete(XEndpoint endpoint) {
          callback.onCallback(SendOutcome.SENT, null);
        }
  
        @Override public void onError(XEndpoint endpoint, Throwable cause) {
          callback.onCallback(SendOutcome.ERROR, cause);
        }

        @Override public void onSkip(XEndpoint endpoint) {
          callback.onCallback(SendOutcome.SKIPPED, null);
        }
      };
    } else {
      return null;
    }
  }
}
