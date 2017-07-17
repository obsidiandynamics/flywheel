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
If you've used MQTT before, you're probably used to dealing with 3rd party client libraries and pushing bytes around, even though most of IoT-style messaging tends to revolve around simple, short text strings. And if you need to use HTTP(S), then most brokers will simply tunnel MQTT over WebSockets anyway. So why not use WebSockets directly?

This is exactly what Flywheel does. Using WebSockets gains natural compatibility with HTTP(S) infrastructure while maintaining the efficiency of the WebSocket protocol - only two bytes of overhead per frame. Dispensing with a heavyweight binary protocol gives you the flexibility of using _any_ WebSocket-compatible client, be it the HTML5 `WebSocket` object native to most browsers, or an equivalent API in mobile devices. The point is that just about every device today supports WebSockets, and it makes for an excellent message streaming protocol. If you need the efficiency of binary messaging, WebSockets support that too, and so Flywheel gives you the ability to publish in both text and binary format natively.

Flywheel builds on MQTT's topic-based subscription model and is syntactically compatible with MQTT topics. Applications utilising MQTT messaging can be ported to Flywheel with no changes to the topic hierarchy and subscription filters. So if you know MQTT, you'll feel at home with Flywheel.

## Flexibility
Embedded and standalone

Auth modules

Plugin API for extending the broker

## Performance and scalability
Flywheel is built on an active-active, scale-out architecture, utilising multiple stateless broker nodes to achieve very high throughput over a large number of connections. It is architected with cloud-first principles, favouring containerised deployments, immutability and PaaS. It's efficient too, utilising asynchronous I/O it can accommodate between 10K-100K connections on a single broker node (depending on the H/W specs). The message router uses the [Indigo](https://github.com/obsidiandynamics/indigo) actor model behind the scenes, and will utilise all the CPU cores you give it.

## Standards compliance
Flywheel sticks to the WebSocket standard, as defined in [RFC-6455](https://tools.ietf.org/html/rfc6455) and, thus, will work with any compliant client implementation.

For message filtering, Flywheel's topic structure is compliant with [MQTT 3.1.1](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718106). 

# Getting Started

