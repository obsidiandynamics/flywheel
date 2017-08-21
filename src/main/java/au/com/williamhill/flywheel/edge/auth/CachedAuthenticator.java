package au.com.williamhill.flywheel.edge.auth;

import java.util.*;
import java.util.concurrent.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.util.*;

public final class CachedAuthenticator extends Thread implements Authenticator {
  private static final class LiveTopics {
    final Map<String, LiveTopic> map = new ConcurrentHashMap<>();
  }
  
  private static final class LiveTopic {
    long expiryTime;

    long getAllowedMillis(long now) {
      return expiryTime == 0 ? 0 : expiryTime - now;
    }
  }
  
  private final Map<EdgeNexus, LiveTopics> nexusTopics = new ConcurrentHashMap<>();
  
  private final Object nexusTopicsLock = new Object();
  
  private final long scanIntervalMillis;
  
  private final NestedAuthenticator delegate;
  
  private AuthConnector connector;
  
  private volatile boolean running = true;
  
  public CachedAuthenticator(long runIntervalMillis, NestedAuthenticator delegate) {
    super(String.format("AuthReaper[runInterval=%dms]", runIntervalMillis));
    this.scanIntervalMillis = runIntervalMillis;
    this.delegate = delegate;
  }
  
  @Override
  public void run() {
    while (running) {
      cycle();
      try {
        Thread.sleep(scanIntervalMillis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        continue;
      }
    }
  }
  
  private void cycle() {
    
  }
  
  private LiveTopic query(EdgeNexus nexus, String topic) {
    final LiveTopics existingTopics = nexusTopics.get(nexus);
    return existingTopics != null ? existingTopics.map.get(topic) : null;
  }
  
  private LiveTopic update(EdgeNexus nexus, String topic) {
    final LiveTopics topics = Maps.putAtomic(nexusTopicsLock, nexusTopics, nexus, LiveTopics::new);
    return Maps.putAtomic(topics, topics.map, topic, LiveTopic::new);
  }
  
  @Override
  public void attach(AuthConnector connector) throws Exception {
    this.connector = connector;
    start();
    delegate.attach(connector);
  }
  
  @Override
  public void close() throws Exception {
    delegate.close();
    running = false;
    interrupt();
    join();
  }

  @Override
  public void verify(EdgeNexus nexus, String topic, AuthenticationOutcome outcome) {
    final long now = System.currentTimeMillis();
    final LiveTopic existing = query(nexus, topic);
    
    final long cachedAllowedMillis = existing != null ? existing.getAllowedMillis(now) : -1;
    
    if (cachedAllowedMillis >= 0) {
      // was cached, and the cached entry is still allowed
      outcome.allow(cachedAllowedMillis);
      return;
    } else {
      // not cached, or the cached entry has expired
      delegate.verify(nexus, topic, new AuthenticationOutcome() {
        @Override
        public void allow(long millis) {
          final LiveTopic liveTopic = update(nexus, topic);
          liveTopic.expiryTime = millis != 0 ? now + millis : 0;
          outcome.allow(millis);
        }

        @Override
        public void deny(TopicAccessError error) {
          outcome.deny(error); 
        }
      });
    }
  }
}
