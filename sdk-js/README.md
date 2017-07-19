SDK-JS
===
JavaScript SDK for Flywheel.

Thoughts on the SDK... will be turned into a working doc when implemented.

# Role of the SDK
* Convenient wrapper around WebSocket
* Object models for `Bind`, `BindResponse`, `Error` (base class) + `General` + `TopicAccess`, `Auth` (base class) + `Basic` + `Bearer`
* Callbacks to handle `onOpen`, `onClose`, `onText`
* Methods to `publishText()`
* Detecting connection termination and automatically reconnecting

# What it might look like
```javascript
// configure the connection
const conf = {
  url: "ws://localhost:8080/broker",
  reconnectBackoffMillis: 1000
};

// create a remote node and initiate the connection
const remoteNode = new RemoteNode(conf)
.onOpen(function(nexus) {
  // Called when a connection has been established (first time, or following a reconnect).
  // Normally this is where you would bind your subscriptions, provide initial auth credentials, etc.
  console.log("Opened " + nexus);

  const bind = {
    subscribe: ["time"],
    auth: {type: "Basic", username: "user", password: "pass"}
  };
  nexus.bind(bind, function(bindResponse) {
    // handle the bind response from the edge node
    if (bindResponse.errors.length !== 0) {
      // some errors occurred... display them and close the remote node (which will also close the current nexus)
      console.log("Binding errors: " + bindResponse.errors);
      remoteNode.close();
    } else {
      console.log("Bind succeeded");
      // bind succeeded... use a timer to publish a string once every second... for as long as the current nexus remains open
      const timer = setInterval(function() {
        if (nexus.isOpen()) {
          console.log("Publishing...");

          // send a P-frame via the nexus
          nexus.publishText("test/topic", "hello there");
        } else {
          clearInterval(timer); 
        }
      }, 1000);
    }
  });
})
.onClose(function(nexus) {
  // Invoked when a connection to the edge node is lost (irrespective of whether we're going to reconnect).
  console.log("Closed " + nexus);
})
.onText(function(nexus, topic, message) {
  // Invoked when an R-frame has been received
  console.log("Received " + message " on topic " + topic);
})
.open(); // open() can only be called once; calling it a second time will have no effect unless close() is called

// ... some time later ...

// eventually when we're done with the node
remoteNode.close();
// closing the remote node should have the added effect of closing the underlying nexus, if one exists and is open
```
