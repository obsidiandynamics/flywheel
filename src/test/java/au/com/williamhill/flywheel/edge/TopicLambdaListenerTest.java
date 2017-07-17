package au.com.williamhill.flywheel.edge;

import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.*;
import org.mockito.*;

import au.com.williamhill.flywheel.edge.TopicLambdaListener.*;
import au.com.williamhill.flywheel.frame.*;

public final class TopicLambdaListenerTest {
  private TopicLambdaListener listener;
  
  private EdgeNexus nexus;
  
  @Before
  public void before() {
    listener = new TopicLambdaListener();
    nexus = new EdgeNexus(null, null);
  }

  @Test
  public void testOnOpen() {
    final OnOpen handler = mock(OnOpen.class);
    listener.onOpen(nexus);
    listener.onOpen(handler);
    listener.onOpen(nexus);
    Mockito.verify(handler).onOpen(Mockito.eq(nexus));
  }

  @Test
  public void testOnClose() {
    final OnClose handler = mock(OnClose.class);
    listener.onClose(nexus);
    listener.onClose(handler);
    listener.onClose(nexus);
    Mockito.verify(handler).onClose(Mockito.eq(nexus));
  }

  @Test
  public void testOnBind() {
    final OnBind handler = mock(OnBind.class);
    final BindFrame bind = new BindFrame();
    final BindResponseFrame bindRes = new BindResponseFrame(new UUID(0, 0));
    listener.onBind(nexus, bind, bindRes);
    listener.onBind(handler);
    listener.onBind(nexus, bind, bindRes);
    Mockito.verify(handler).onBind(Mockito.eq(nexus), Mockito.eq(bind), Mockito.eq(bindRes));
  }

  @Test
  public void testOnPublishText() {
    final OnPublishText handler = mock(OnPublishText.class);
    final PublishTextFrame pub = new PublishTextFrame("test", "message");
    listener.onPublish(nexus, pub);
    listener.onPublishText(handler);
    listener.onPublish(nexus, pub);
    Mockito.verify(handler).onPublish(Mockito.eq(nexus), Mockito.eq(pub));
  }

  @Test
  public void testOnPublishBinary() {
    final OnPublishBinary handler = mock(OnPublishBinary.class);
    final PublishBinaryFrame pub = new PublishBinaryFrame("test", "message".getBytes());
    listener.onPublish(nexus, pub);
    listener.onPublishBinary(handler);
    listener.onPublish(nexus, pub);
    Mockito.verify(handler).onPublish(Mockito.eq(nexus), Mockito.eq(pub));
  }
}
