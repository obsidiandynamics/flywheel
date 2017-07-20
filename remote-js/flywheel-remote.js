/**
 * Represents a single connection initiated by the remote node, to the edge node. Also
 * encapsulates a WebSocket object for sending and receiving messages.
 */
class Nexus {
  constructor(remote, ws) {
    this.remote = remote;
    this.ws = ws;
    this.pendingBindRequests = {};
  }
  
  /**
   * Sends a bind request to the edge node, invoking the given callback upon
   * receiving the response.
   * @param bind The bind payload.
   * @param {function(bindResponse)} bindCallback Callback to handle the response message.
   */
  bind(bind, bindCallback) {
    const generateUuid = () =>
      'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
        const r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
      });
    
    bind.messageId = bind.messageId || generateUuid();
    this.pendingBindRequests[bind.messageId] = bindCallback;
    this.ws.send(`B ${JSON.stringify(bind)}`);
  }
  
  /**
   * Publishes a text message over this nexus, if the underlying WebSocket is live
   * (isn't closed or closing).
   */
  publishText(topic, message) {
    if (this._isLive()) {
      this.ws.send(`P ${topic} ${message}`);
    }
  }
  
  /**
   * Closes the current nexus. However, if the reconnect backoff was set, the SDK will 
   * eventually reconnect, creating a brand new Nexus object. 
   * To close the remote permanently without incurring a reconnect, call RemoteNode.close()
   * instead.
   */
  close() {
    if (this._isLive()) {
      this.ws.close();
    }
  }
  
  _isLive() {
    return this.ws.readyState !== this.ws.CLOSING && this.ws.readyState != this.ws.CLOSED;
  }
}

/**
 * Represents the remote node, encapsulating a single nexus connection to an edge node.
 */
class RemoteNode {
  _handleOpen(nexus, event) {
    if (this.onOpenHandler !== undefined) this.onOpenHandler(nexus, event);
  }

  _handleClose(nexus, event) {
    if (this.reconnectBackoffMillis !== undefined) {
      // the backoff time was set... wait the requested interval and reconnect
      const retry = setTimeout(() => {
        if (this.reconnectBackoffMillis !== undefined) {
          // second 'if' check required in case close() was called before the timer fired
          this._openNexus();
        }
        clearTimeout(retry);
      }, this.reconnectBackoffMillis);
    } else {
      this.close();
    }
    if (this.onCloseHandler !== undefined) this.onCloseHandler(nexus, event);
  }
  
  _handleMessage(nexus, event) {
    const data = event.data;
    if (data.length > 2) {
      const type = data.charAt(0);
      switch (type) {
        case 'B':
          const bindResJson = data.substring(2);
          const bindRes = JSON.parse(bindResJson);
          if (this.log) console.log({log: `RemoteNode._handleMessage(): received B-frame`, bindRes: bindResJson});
          const pendingBind = nexus.pendingBindRequests[bindRes.messageId];
          if (pendingBind !== undefined) {
            delete nexus.pendingBindRequests[bindRes.messageId];
            pendingBind(bindRes);
          } else {
            console.warn({log: `RemoteNode._handleMessage(): no pending bind request for message with ID ${bindRes.messageId}`});
          }
          break;

        case 'R':
          const index = data.indexOf(' ', 2);
          if (index !== -1) {
            const topic = data.substring(2, index);
            const message = data.substring(index + 1);
            if (this.log) console.log({log: `RemoteNode._handleMessage(): received R-frame`, topic: topic, message: message});
            if (this.onTextHandler !== undefined) this.onTextHandler(nexus, topic, message);
          } else {
            console.warn({log: `RemoteNode._handleMessage(): no delimiter in R-frame '${data}'`});
          }
          break;

        default:
          console.error({log: `RemoteNode._handleMessage(): unsupported type code '${type}'`});
          break;
      }
    } else {
      console.error({log: `RemoteNode._handleMessage(): received invalid message data '${data}'`});
    }
  }
  
  _openNexus() {
    const nexus = new Nexus(this, new WebSocket(this.url));
    this.nexus = nexus;
    this.nexus.ws.onopen = event => this._handleOpen(nexus, event);
    this.nexus.ws.onclose = event => this._handleClose(nexus, event);
    this.nexus.ws.onmessage = event => this._handleMessage(nexus, event);
  }
  
  /**
   * Opens a nexus to the edge node and (optionally) keeps it open by reconnecting when
   * required. If a nexus is already open, this method does nothing.
   * @param conf The configuration.
   */
  open(conf) {
    if (this.log) console.log({log: `RemoteNode.open()`});
    if (this.nexus === undefined) {
      console.assert(conf.url !== undefined, "No value for property 'url'");
      this.reconnectBackoffMillis = conf.reconnectBackoffMillis;
      this.url = conf.url;
      this.log = conf.log;
      this._openNexus();
    }
    return this;
  }
  
  /**
   * Permanently closes the underlying nexus connection and cleans up, preventing any further reconnects. 
   * If the nexus is already closed, this method does nothing.
   */
  close() {
    if (this.log) console.log({log: `RemoteNode.close()`});
    if (this.nexus !== undefined) {
      delete this.url;
      delete this.reconnectBackoffMillis;
      delete this.log;
      this.nexus.close();
      delete this.nexus;
    }
  }
  
  /**
   * Registers an on-open handler.
   * @param {function(nexus, wsEvent)} onOpenHandler The handler callback.
   */
  onOpen(onOpenHandler) {
    this.onOpenHandler = onOpenHandler;
    return this;
  }
  
  /**
   * Registers an on-close handler, invoked when the current nexus is closed, or when a
   * connection error occurs.
   * @param {function(nexus, wsEvent)} onCloseHandler The handler callback.
   */
  onClose(onCloseHandler) {
    this.onCloseHandler = onCloseHandler;
    return this;
  }
  
  /**
   * Registers an on-text handler, invoked when a text message is received.
   * @param {function(nexus, topic, message)} onTextHandler The handler callback.
   */
  onText(onTextHandler) {
    this.onTextHandler = onTextHandler;
    return this;
  }
  
  /**
   * Publishes a text message over the underlying nexus.
   */
  publishText(topic, message) {
    if (this.nexus !== undefined) {
      this.nexus.publishText(topic, message);
    }
  }
}