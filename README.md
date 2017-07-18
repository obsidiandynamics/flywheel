Flywheel
===
[ ![Download](https://api.bintray.com/packages/william-hill-community/flywheel/flywheel-core/images/download.svg) ](https://bintray.com/william-hill-community/flywheel/flywheel-core/_latestVersion)

Flywheel is a simple broker for secure pub/sub message streaming over WebSockets. It can run as a standalone cluster or embedded into an existing application. It's easy to extend and supports custom auth'n/auth'z plugins for controlling exactly what your subscribers get access to.

# Why Flywheel
With the already excellent MQTT we're spoiled for choice when it comes to high-speed, low-latency messaging with high fan-in/out topologies. And then you have SaaS-based solutions such as Amazon's IoT. So why Flywheel?

## Security
These solutions work well when the _client_ controls what data they wish to receive. If you need to enforce control at the broker level, you're after _authorization_. This is where things get really complicated, really fast. MQTT, AWS IoT and others allow you to define static auth'z policies, specifying up-front which _devices_ can access which topics (usually through some sort of ACLs and pattern matching). If you can't control how end-users map to physical devices, or if you have to re-evaluate an end-user's privileges at an arbitrary point in time (for example, when allowing for initially anonymous connections that can optionally authenticate later to gain more subscriptions), you're out of luck.

Flywheel auth'z model is dynamic, allowing you to define the behaviour by writing a little bit of code. This gives the broker control over when the security policy is evaluated and gives the client the ability to present their credentials in a variety of ways. Flywheel has equivalents for HTTP Basic Auth and Bearer Auth, supporting anything from a basic username/password combo to a JWT tokens, Kerberos tickets, and so on.

## Simplicity
If you've used MQTT before, you're probably used to dealing with 3rd party client libraries and pushing bytes around, even though most of IoT-style messaging tends to revolve around simple, concise text strings. And if you need to use HTTP(S), then most brokers will simply tunnel MQTT over WebSockets anyway. So why not use WebSockets directly?

This is exactly what Flywheel does. Using WebSockets preserves natural compatibility with HTTP(S) infrastructure while gaining the efficiency of the WebSocket protocol - full-duplex messaging with only two bytes of overhead per frame. Dispensing with a heavyweight binary protocol gives you the flexibility of using _any_ WebSocket-compatible client, be it the HTML5 `WebSocket` object native to most browsers, or an equivalent API in mobile devices. The point is that just about every device today supports WebSockets, and it makes for an excellent message streaming protocol. If you need the efficiency of binary messaging, WebSockets support that too, and so Flywheel gives you the ability to publish in both text and binary format natively, without resorting to base-64.

Flywheel builds on MQTT's topic-based subscription model and is syntactically compatible with MQTT topics. Applications utilising MQTT messaging can be ported to Flywheel with no changes to the topic hierarchy and subscription filters. So if you know MQTT, you'll feel at home with Flywheel.

## Flexibility
Flywheel can run in either embedded or standalone mode. In embedded mode, the Flywheel broker runs in-process, letting you add message streaming support to a single JVM-based application in a couple of minutes - just import the library and add a few lines of code to configure and start the server. 

Standalone mode is used to run one or more dedicated clusters of broker nodes to enable a message streaming capability for an entire organisation, with multiple message publishers and subscribers.

## Extensibility
Flywheel was born out of a need for proper configurability and extensibility, particularly in the area of subscription control. It is built around a modular design, and supports two kinds of extension points:

* Auth modules: apply custom auth'n and auth'z behaviour at any point in the topic hierarchy. Multiple auth modules can co-exist, allowing you to apply a different policy depending on the topic.
* Plugins: a general purpose extension, hosting custom code within the broker without altering the broker implementation. Plugins can be used to enhance the capabilities of the broker, for example, adding metrics and monitoring support.

## Messaging @ scale
Flywheel is built on an active-active, scale-out architecture, utilising multiple stateless broker nodes to achieve very high throughput over a large number of connections. It's architected around the cloud, favouring containerised deployments, immutability and PaaS. It's efficient too, utilising asynchronous I/O it can accommodate between 10K-100K connections on a single broker node (depending on the H/W specs). Message routing is fully parallel, utilising all available CPU cores.


# Architecture
Some brief terminology first.

* **Remote Node** - A client of the broker that can publish messages and receive messages (the latter requiring a prior subscription to one or more topics). The remote node may connect to any **Edge Node** in a broker cluster via a WebSocket connection.
* **Edge Node** - A single broker instance that can be deployed in a centralised cluster (close to its peer edge nodes) or in geographically distributed locations (close to the remote nodes).
* **Backplane** - A high throughput interconnect between the edge nodes, making messages uniformly available to all edge nodes in a cluster, irrespective of which edge node they were published to.
* **Nexus** - A single connection between a remote node and an edge node.

The diagram below describes a typical Flywheel topology.

<img src="https://raw.githubusercontent.com/wiki/William-Hill-Community/flywheel/images/flywheel-architecture-simple.svg" alt="logo" width="500"/>


# Getting Started
The first step is deciding on which of the two modes - embedded or standalone - is best-suited to your messaging scenario.

## Embedded mode
In this example we'll start an edge node on port `8080`, serving a WebSocket on path `/`.

### Get the binaries
Gradle builds are hosted on JCenter. Just add the following snippet to your build file (replacing the version number in the snippet with the version shown on the Download badge at the top of this README).

For Maven:

```xml
<dependency>
  <groupId>au.com.williamhill.flywheel</groupId>
  <artifactId>flywheel-core</artifactId>
  <version>0.1.0</version>
  <type>pom</type>
</dependency>
```

For Gradle:

```groovy
compile 'au.com.williamhill.flywheel:flywheel-core:0.1.0'
```

### Configure and start the edge node
```java
EdgeNode.builder()
.withServerConfig(new XServerConfig()
                  .withPath("/")
                  .withPort(8080))
.build();
```

That's all there is to it. You should now have a WebSocket broker listening on `ws://localhost:8080/`. Try it out with this [sample WebSocket client](http://websocket.org/echo.html). 

The above snippet will return an instance of `EdgeNode`, which you can use to publish messages directly. Simply call one of `EdgeNode.publish(String topic, String payload)` or `EdgeNode.publish(String topic, byte[] payload)` to publish a text or binary message respectively on the given topic.

Listening to connection states as well as published messages can be done by providing a `TopicListener` implementation:
```java
edge.addTopicListener(new TopicListener() {
  @Override public void onOpen(EdgeNexus nexus) {}
  @Override public void onClose(EdgeNexus nexus) {}
  @Override public void onBind(EdgeNexus nexus, BindFrame bind, BindResponseFrame bindRes) {}
  @Override public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {}
  @Override public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {}
});
```

If you only care about one or two specific event types, use a composable `TopicLambdaListener`, feeding it just the necessary Lambda expressions, in the form:
```java
edge.addTopicListener(new TopicLambdaListener()
                      .onOpen(nexus -> {})
                      .onClose(nexus -> {})
                      .onBind((nexus, bind, bindRes) -> {})
                      .onPublishText((nexus, pub) -> {})
                      .onPublishBinary((nexus, pub) -> {}));
```

Alternatively, you can you can subclass `TopicLambdaListener` directly, overriding just the methods you need.