package au.com.williamhill.flywheel;

import static org.junit.Assert.*;

import java.nio.*;
import java.util.concurrent.*;

import org.junit.*;
import org.mockito.*;

import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.frame.Wire.*;
import au.com.williamhill.flywheel.socketx.*;

public final class SendHelperTest {
  @Test
  public void testConformance() throws Exception {
    TestSupport.assertUtilityClassWellDefined(SendHelper.class);
  }
  
  @Test
  public void testWrapCallbackNull() {
    assertNull(SendHelper.wrapCallback(null));
  }
  
  @Test
  public void testWrapCallbackNonNull() {
    final SendCallback sendCallback = Mockito.mock(SendCallback.class);
    final XEndpoint endpoint = Mockito.mock(XEndpoint.class);
    final XSendCallback xSendCallback = SendHelper.wrapCallback(sendCallback);
    assertNotNull(xSendCallback);
    
    xSendCallback.onComplete(endpoint);
    Mockito.verify(sendCallback).onCallback(Mockito.eq(SendOutcome.SENT), Mockito.isNull(Throwable.class));
    
    final Throwable cause = new Throwable();
    xSendCallback.onError(endpoint, cause);
    Mockito.verify(sendCallback).onCallback(Mockito.eq(SendOutcome.ERROR), Mockito.eq(cause));
    
    xSendCallback.onSkip(endpoint);
    Mockito.verify(sendCallback).onCallback(Mockito.eq(SendOutcome.SKIPPED), Mockito.isNull(Throwable.class));
  }
  
  @Test
  public void testWrapFutureOnComplete() throws InterruptedException, ExecutionException {
    final CompletableFuture<SendOutcome> f = new CompletableFuture<>();
    final XEndpoint endpoint = Mockito.mock(XEndpoint.class);
    final XSendCallback xSendCallback = SendHelper.wrapFuture(f);
    assertNotNull(xSendCallback);
    
    xSendCallback.onComplete(endpoint);
    assertTrue(f.isDone());
    assertFalse(f.isCompletedExceptionally());
    assertEquals(SendOutcome.SENT, f.get());
  }
  
  @Test
  public void testWrapFutureOnError() throws InterruptedException {
    final CompletableFuture<SendOutcome> f = new CompletableFuture<>();
    final XEndpoint endpoint = Mockito.mock(XEndpoint.class);
    final XSendCallback xSendCallback = SendHelper.wrapFuture(f);
    assertNotNull(xSendCallback);

    final Throwable cause = new Throwable();
    xSendCallback.onError(endpoint, cause);
    assertTrue(f.isCompletedExceptionally());
    try {
      f.get();
      fail("Expected exception");
    } catch (ExecutionException e) {
      assertEquals(cause, e.getCause());
    }
  }
  
  @Test
  public void testWrapFutureOnSkip() throws InterruptedException, ExecutionException {
    final CompletableFuture<SendOutcome> f = new CompletableFuture<>();
    final XEndpoint endpoint = Mockito.mock(XEndpoint.class);
    final XSendCallback xSendCallback = SendHelper.wrapFuture(f);
    assertNotNull(xSendCallback);
    
    xSendCallback.onSkip(endpoint);
    assertTrue(f.isDone());
    assertEquals(SendOutcome.SKIPPED, f.get());
  }
  
  @Test
  public void testSendAutoTextCallback() {
    final SendCallback sendCallback = Mockito.mock(SendCallback.class);
    final XEndpoint endpoint = Mockito.mock(XEndpoint.class);
    SendHelper.sendAuto(new TextFrame("topic", "message"), endpoint, getWire(), sendCallback);
    Mockito.verify(endpoint).send(Mockito.anyString(), Mockito.isNotNull(XSendCallback.class));
  }
  
  @Test
  public void testSendAutoBinaryCallback() {
    final SendCallback sendCallback = Mockito.mock(SendCallback.class);
    final XEndpoint endpoint = Mockito.mock(XEndpoint.class);
    SendHelper.sendAuto(new BinaryFrame("topic", "message".getBytes()), endpoint, getWire(), sendCallback);
    Mockito.verify(endpoint).send(Mockito.isNotNull(ByteBuffer.class), Mockito.isNotNull(XSendCallback.class));
  }
  
  @Test
  public void testSendAutoTextFuture() {
    final XEndpoint endpoint = Mockito.mock(XEndpoint.class);
    final CompletableFuture<SendOutcome> f = SendHelper.sendAuto(new TextFrame("topic", "message"), endpoint, getWire());
    assertNotNull(f);
    Mockito.verify(endpoint).send(Mockito.anyString(), Mockito.isNotNull(XSendCallback.class));
  }
  
  @Test
  public void testSendAutoBinaryFuture() {
    final XEndpoint endpoint = Mockito.mock(XEndpoint.class);
    final CompletableFuture<SendOutcome> f = SendHelper.sendAuto(new BinaryFrame("topic", "message".getBytes()), endpoint, getWire());
    assertNotNull(f);
    Mockito.verify(endpoint).send(Mockito.isNotNull(ByteBuffer.class), Mockito.isNotNull(XSendCallback.class));
  }
  
  private static Wire getWire() {
    return new Wire(false, LocationHint.UNSPECIFIED);
  }
}
