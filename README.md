Flywheel
===
[ ![Download](https://api.bintray.com/packages/william-hill-community/flywheel/flywheel-core/images/download.svg) ](https://bintray.com/william-hill-community/flywheel/flywheel-core/_latestVersion)

Flywheel is a simple broker for secure pub/sub message streaming over WebSockets. It can run as a standalone cluster or embedded into an existing application. It's easy to extend and supports custom auth'n/auth'z plugins for controlling exactly what your subscribers get access to.

# Why Flywheel
With the already excellent MQTT we're spoiled for choice when it comes to high-speed, low-latency messaging with high fan-in/out topologies. And then you have SaaS-based solutions such as Amazon's IoT. So why Flywheel?

These solutions work well when the _client_ controls what data they want to receive. If you want to enforce control at the broker level, you need _authorization_. This is where things get really complicated, really fast. MQTT, AWS IoT and others allow you to define static auth'z policies, specifying up-front which _devices_ can access which topics (usually through some sort of ACLs and pattern matching). If you don't control how end-users map to physical devices, or if you have to re-evaluate an end-user's privileges at an arbitrary point in time (for example, when allowing for initially anonymous connections that can optionally authenticate later to gain more subscriptions), you're out of luck.

Flywheel auth'z model is dynamic, allowing you to define the behaviour by writing a bit of code. This gives the broker control over when the security policy is evaluated and gives the client the ability to present their credentials in a variety of ways. Flywheel has equivalents for HTTP Basic Auth and Bearer Auth, supporting anything from a basic username/password combo to a JWT tokens, Kerberos tickets, and so on.

# Getting Started

