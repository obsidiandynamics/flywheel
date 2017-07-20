Flywheel - Remote SDK for JavaScript
===
JavaScript SDK for HTML5 compliant web browsers.

# About
Given the simplicity of the protocol, you don't need to use a library when connecting to Flywheel - as long as you're willing to deal with the HTML5 `WebSocket` and perform some trivial string manipulation to handle `B`, `P` and `R` frames. This library saves you the hassle, providing -

* A convenient wrapper around the HTML5 `WebSocket` object;
* Event logging of `WebSocket` life-cycle events and message receival;
* Flywheel wire protocol - (un)marshaling of text frames and delegating to the appropriate handler;
* Connection maintenance - detecting connection failure and automatically reconnecting after a set interval.

# Limitations

* Only the text protocol is currently supported.

# Usage
The example below instantiates a remote node and opens a nexus to `ws://localhost:8080/broker`, with the reconnection option set. It then subscribes to the `time` and `communal` topics, and after binding will publishes a single `Hello world!` text message over the `communal` topic. After ten seconds of messaging activity, the remote node will permanently close the nexus.

Note: this assumes that `flywheel-remote.js` has been imported into your project.

```javascript
// configure the remote node
const conf = {
  // mandatory - the WebSocket endpoint URL
  url: "ws://localhost:8080/broker", 
  
  // optional 
  // - if set, the remote node will maintain the connection open with a given backoff 
  // - otherwise the connection will be a one-off
  reconnectBackoffMillis: 1000,
  
  // optional - if set, connection events and message receival will be logged
  log: true
};

// create a remote node and initiate the connection
const remoteNode = new RemoteNode()
.onOpen((nexus, wsEvent) => {
  // Called when a connection has been established (first time, or following a reconnect).
  // Normally this is where you would bind your subscriptions, provide initial auth credentials, etc.
  console.log(`Opened ${nexus}`);
  
  const bind = {
    subscribe: ["time", "communal"],
    auth: {type: "Basic", username: "user", password: "pass"}
  };
  nexus.bind(bind, bindResponse => {
    console.log(`Bind response: ${bindResponse}`);
    // handle the bind response from the edge node
    if (bindResponse.errors.length !== 0) {
      // some errors occurred... display them and close the remote node (which will also close the current nexus)
      console.log(`Binding errors: ${JSON.stringify(bindResponse.errors)}`);
      nexus.remote.close();
    } else {
      // no errors... publish a single 'Hello world!' message (which we will also receive ourselves)
      nexus.publishText("communal", "Hello world!");
    }
  });
})
.onClose((nexus, wsEvent) => {
  console.log(`Closed ${nexus}, code: ${wsEvent.code}, reason: ${wsEvent.reason}`);
})
.open(conf); // open() can only be called once; calling it a second time will have no effect unless close() is called

const someTimeLater = setTimeout(() => {
  remoteNode.close(); // calling close() will close the nexus and stop any further reconnection attempts
  clearTimeout(someTimeLater);
}, 10000);
```
