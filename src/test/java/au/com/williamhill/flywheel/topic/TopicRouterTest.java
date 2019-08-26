package au.com.williamhill.flywheel.topic;

import static junit.framework.TestCase.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;

import com.obsidiandynamics.indigo.*;
import com.obsidiandynamics.indigo.util.*;

import au.com.williamhill.flywheel.util.*;

public final class TopicRouterTest implements TestSupport {
  private ActorSystem system;
  
  private TopicWatcher topicWatcher;
  
  @Before
  public void setup() {
    topicWatcher = mock(TopicWatcher.class);
    system = ActorSystem.create()
    .on(TopicRouter.ROLE).cue(() -> new TopicRouter(new TopicConfig() {{
      topicWatcher = InterceptingProxy.of(TopicRouterTest.this.topicWatcher, new LoggingInterceptor<>());
    }}));
  }
  
  @After
  public void teardown() {
    system.shutdownSilently();
  }
  
  @Test
  public void testNonInterfering() throws InterruptedException, ExecutionException {
    final List<Delivery> aList = new ArrayList<>();
    final List<Delivery> bList = new ArrayList<>();
    subscribe("a", aList::add);
    subscribe("b", bList::add);
    Ordered.of(topicWatcher, inOrder -> {
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("b")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("b")), notNull());
    });
    publish("a", "hello").get();
    publish("b", "barev").get();
    assertEquals(1, aList.size());
    assertEquals(1, bList.size());
    assertEquals("hello", aList.get(0).getPayload());
    assertEquals("barev", bList.get(0).getPayload());
  }
  
  @Test
  public void testMultipleSubscribers() throws InterruptedException, ExecutionException {
    final List<Delivery> a1List = new ArrayList<>();
    final List<Delivery> a2List = new ArrayList<>();
    subscribe("a", a1List::add);
    subscribe("a", a2List::add);
    Ordered.of(topicWatcher, inOrder -> {
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a")));
      inOrder.verify(topicWatcher, times(2)).subscribed(notNull(), eq(Topic.of("a")), notNull());
    });
    publish("a", "hello").get();
    assertEquals(1, a1List.size());
    assertEquals(1, a2List.size());
    assertEquals("hello", a1List.get(0).getPayload());
    assertEquals("hello", a2List.get(0).getPayload());
  }
  
  @Test
  public void testDuplicateSubscribers() throws InterruptedException, ExecutionException {
    final List<Delivery> aList = new ArrayList<>();
    final Subscriber sub = aList::add;
    subscribe("a", sub);
    subscribe("a", sub);
    Ordered.of(topicWatcher, inOrder -> {
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a")), notNull());
    });
    publish("a", "hello").get();
    assertEquals(1, aList.size());
    assertEquals("hello", aList.get(0).getPayload());
  }
  
  @Test
  public void testNoSubscribers() {
    publish("a", "hello");
  }
  
  @Test
  public void testHierarchyBasic() throws InterruptedException, ExecutionException {
    final List<Delivery> aList = new ArrayList<>();
    final List<Delivery> abList = new ArrayList<>();
    final List<Delivery> abcList = new ArrayList<>();
    subscribe("a", aList::add);
    subscribe("a/b", abList::add);
    subscribe("a/b/c", abcList::add);
    Ordered.of(topicWatcher, inOrder -> {
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a/b")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a/b")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a/b/c")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a/b/c")), notNull());
    });
    publish("a", "hello").get();
    publish("a/b", "barev").get();
    publish("a/b/c", "ciao").get();
    assertEquals(1, aList.size());
    assertEquals(1, abList.size());
    assertEquals(1, abcList.size());
    assertEquals("hello", aList.get(0).getPayload());
    assertEquals("barev", abList.get(0).getPayload());
    assertEquals("ciao", abcList.get(0).getPayload());
  }
  
  @Test
  public void testHierarchyMultiLevelWildcard() throws InterruptedException, ExecutionException {
    final List<Delivery> xList = new ArrayList<>();
    final List<Delivery> abList = new ArrayList<>();
    final List<Delivery> axList = new ArrayList<>();
    final List<Delivery> cList = new ArrayList<>();
    final List<Delivery> cxList = new ArrayList<>();
    subscribe("#", xList::add);
    subscribe("a/b", abList::add);
    subscribe("a/#", axList::add);
    subscribe("c", cList::add);
    subscribe("c/#", cxList::add);
    Ordered.of(topicWatcher, inOrder -> {
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("#")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a/b")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a/b")), notNull());
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a/#")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("c")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("c")), notNull());
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("c/#")), notNull());
    });
    publish("a", "hello").get();
    publish("a/b", "barev").get();
    publish("c", "ciao").get();
    publish("c/p", "privet").get();
    publish("c/p/b", "privet vsem").get();
    assertEquals(5, xList.size());
    assertEquals(1, abList.size());
    assertEquals(1, abList.size());
    assertEquals(1, axList.size());
    assertEquals(1, cList.size());
    assertEquals(2, cxList.size());
    assertEquals("hello", xList.get(0).getPayload());
    assertEquals("barev", xList.get(1).getPayload());
    assertEquals("ciao", xList.get(2).getPayload());
    assertEquals("privet", xList.get(3).getPayload());
    assertEquals("privet vsem", xList.get(4).getPayload());
    assertEquals("barev", abList.get(0).getPayload());
    assertEquals("barev", axList.get(0).getPayload());
    assertEquals("ciao", cList.get(0).getPayload());
    assertEquals("privet", cxList.get(0).getPayload());
    assertEquals("privet vsem", cxList.get(1).getPayload());
  }
  
  @Test
  public void testHierarchySingleLevelWildcard() throws InterruptedException, ExecutionException {
    final List<Delivery> xList = new ArrayList<>();
    final List<Delivery> abList = new ArrayList<>();
    final List<Delivery> xbList = new ArrayList<>();
    final List<Delivery> xcList = new ArrayList<>();
    final List<Delivery> axList = new ArrayList<>();
    final List<Delivery> xxList = new ArrayList<>();
    subscribe("+", xList::add);
    subscribe("a/b", abList::add);
    subscribe("+/b", xbList::add);
    subscribe("+/c", xcList::add);
    subscribe("a/+", axList::add);
    subscribe("+/+", xxList::add);
    Ordered.of(topicWatcher, inOrder -> {
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("+")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a/b")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a/b")), notNull());
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("+/b")), notNull());
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("+/c")), notNull());
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a/+")), notNull());
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("+/+")), notNull());
    });
    publish("a", "hello").get();
    publish("a/b", "barev").get();
    publish("a/c", "ciao").get();
    assertEquals(1, xList.size());
    assertEquals(1, abList.size());
    assertEquals(1, xbList.size());
    assertEquals(1, xcList.size());
    assertEquals(2, axList.size());
    assertEquals(2, xxList.size());
    assertEquals("hello", xList.get(0).getPayload());
    assertEquals("barev", abList.get(0).getPayload());
    assertEquals("barev", xbList.get(0).getPayload());
    assertEquals("ciao", xcList.get(0).getPayload());
    assertEquals("barev", axList.get(0).getPayload());
    assertEquals("ciao", axList.get(1).getPayload());
    assertEquals("barev", xxList.get(0).getPayload());
    assertEquals("ciao", xxList.get(1).getPayload());
  }
  
  @Test
  public void testHierarchyUnsubscribeBottomUp() throws InterruptedException, ExecutionException {
    final List<Delivery> list = new ArrayList<>();
    final Subscriber sub = list::add;
    subscribe("a0", sub);
    subscribe("a1", sub);
    subscribe("a0/b0", sub);
    subscribe("a0/b1", sub);
    subscribe("a0/b0/c0", sub);
    
    publishSelf("a0").get();
    publishSelf("a1").get();
    publishSelf("a0/b0").get();
    publishSelf("a0/b1").get();
    publishSelf("a0/b0/c0").get();
    assertEquals(5, list.size());
    assertEquals(5, new HashSet<>(list).size());
    
    unsubscribe("a0/b0/c0", sub);
    unsubscribe("a0/b1", sub);
    unsubscribe("a0/b0", sub);
    unsubscribe("a1", sub);
    unsubscribe("a0", sub);
    Ordered.of(topicWatcher, inOrder -> {
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a0")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a0")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a1")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a1")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a0/b0")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a0/b0")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a0/b1")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a0/b1")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a0/b0/c0")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a0/b0/c0")), notNull());
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a0/b0/c0")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a0/b0/c0")));
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a0/b1")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a0/b1")));
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a0/b0")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a0/b0")));
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a1")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a1")));
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a0")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a0")));
    });
    
    list.clear();
    publishSelf("a0").get();
    publishSelf("a1").get();
    publishSelf("a0/b0").get();
    publishSelf("a0/b1").get();
    publishSelf("a0/b0/c0").get();
    assertEquals(0, list.size());
  }
  
  @Test
  public void testHierarchyUnsubscribeTopDown() throws InterruptedException, ExecutionException {
    final List<Delivery> list = new ArrayList<>();
    final Subscriber sub = list::add;
    subscribe("a0", sub);
    subscribe("a1", sub);
    subscribe("a0/b0", sub);
    subscribe("a0/b1", sub);
    subscribe("a0/b0/c0", sub);
    
    publishSelf("a0").get();
    publishSelf("a1").get();
    publishSelf("a0/b0").get();
    publishSelf("a0/b1").get();
    publishSelf("a0/b0/c0").get();
    assertEquals(5, list.size());
    assertEquals(5, new HashSet<>(list).size());
    
    unsubscribe("a0", sub);
    unsubscribe("a1", sub);
    unsubscribe("a0/b0", sub);
    unsubscribe("a0/b1", sub);
    unsubscribe("a0/b0/c0", sub);
    Ordered.of(topicWatcher, inOrder -> {
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a0")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a0")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a1")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a1")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a0/b0")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a0/b0")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a0/b1")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a0/b1")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a0/b0/c0")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a0/b0/c0")), notNull());
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a0")), notNull());
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a1")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a1")));
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a0/b0")), notNull());
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a0/b1")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a0/b1")));
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a0/b0/c0")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a0/b0/c0")));
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a0/b0")));
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a0")));
    });
    
    list.clear();
    publishSelf("a0").get();
    publishSelf("a1").get();
    publishSelf("a0/b0").get();
    publishSelf("a0/b1").get();
    publishSelf("a0/b0/c0").get();
    assertEquals(0, list.size());
  }
  
  @Test
  public void testHierarchyUnsubscribeMultiLevelWildcard() throws InterruptedException, ExecutionException {
    final List<Delivery> list = new ArrayList<>();
    final Subscriber sub = list::add;
    subscribe("#", sub);
    subscribe("a/b", sub);
    subscribe("a/#", sub);
    subscribe("c", sub);
    subscribe("c/#", sub);
    
    publishSelf("a").get();
    assertEquals(1, list.size());
    publishSelf("a/b").get();
    assertEquals(4, list.size());
    publishSelf("c").get();
    assertEquals(6, list.size());
    publishSelf("c/d").get();
    assertEquals(8, list.size());
    
    unsubscribe("#", sub);
    unsubscribe("a/b", sub);
    unsubscribe("a/#", sub);
    unsubscribe("c", sub);
    unsubscribe("c/#", sub);
    
    Ordered.of(topicWatcher, inOrder -> {
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("#")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a")));
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a/b")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a/b")), notNull());
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a/#")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("c")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("c")), notNull());
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("c/#")), notNull());
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("#")), notNull());
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a/b")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a/b")));
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a/#")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a")));
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("c")), notNull());
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("c/#")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("c")));
    });
    
    list.clear();
    publishSelf("a").get();
    publishSelf("a/b").get();
    publishSelf("c").get();
    publishSelf("c/d").get();
    assertEquals(0, list.size());
  }
  
  @Test
  public void testHierarchyUnsubscribeSingleLevelWildcard() throws InterruptedException, ExecutionException {
    final List<Delivery> list = new ArrayList<>();
    final Subscriber sub = list::add;
    subscribe("+", sub);
    subscribe("a/b", sub);
    subscribe("+/b", sub);
    subscribe("+/c", sub);
    subscribe("a/+", sub);
    subscribe("+/+", sub);

    unsubscribe("+", sub);
    unsubscribe("a/b", sub);
    unsubscribe("+/b", sub);
    unsubscribe("+/c", sub);
    unsubscribe("a/+", sub);
    unsubscribe("+/+", sub);
    
    Ordered.of(topicWatcher, inOrder -> {
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("+")), notNull());
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a")));
      inOrder.verify(topicWatcher).created(notNull(), eq(Topic.of("a/b")));
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a/b")), notNull());
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("+/b")), notNull());
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("+/c")), notNull());
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("a/+")), notNull());
      inOrder.verify(topicWatcher).subscribed(notNull(), eq(Topic.of("+/+")), notNull());
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("+")), notNull());
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a/b")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a/b")));
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("+/b")), notNull());
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("+/c")), notNull());
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("a/+")), notNull());
      inOrder.verify(topicWatcher).deleted(notNull(), eq(Topic.of("a")));
      inOrder.verify(topicWatcher).unsubscribed(notNull(), eq(Topic.of("+/+")), notNull());
    });
    
    publishSelf("a").get();
    publishSelf("a/b").get();
    publishSelf("a/c").get();
    assertEquals(0, list.size());
  }
  
  private void subscribe(String topic, Subscriber subscriber) throws InterruptedException, ExecutionException {
    final CompletableFuture<?> f = system.ask(ActorRef.of(TopicRouter.ROLE), new Subscribe(Topic.of(topic), subscriber));
    f.get();
    system.drain(0);
  }
  
  private void unsubscribe(String topic, Subscriber subscriber) throws InterruptedException, ExecutionException {
    final CompletableFuture<?> f =  system.ask(ActorRef.of(TopicRouter.ROLE), new Unsubscribe(Topic.of(topic), subscriber));
    f.get();
    system.drain(0);
  }
  
  private CompletableFuture<PublishResponse> publishSelf(String topic) {
    return publish(topic, topic);
  }
  
  private CompletableFuture<PublishResponse> publish(String topic, Object payload) {
    return system.ask(ActorRef.of(TopicRouter.ROLE), new Publish(Topic.of(topic), payload));
  }
}
