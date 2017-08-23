package au.com.williamhill.flywheel.edge.auth;

import java.util.*;
import java.util.concurrent.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.util.*;

public final class CachedAuthenticator extends Thread implements Authenticator {
  private static final class ActiveTopics {
    final Map<String, ActiveTopic> map = new ConcurrentHashMap<>();
  }
  
  private static final class ActiveTopic {
    long expiryTime;

    long getAllowedMillis(long now) {
      return expiryTime == 0 ? 0 : expiryTime - now;
    }
  }
  
  private final Map<EdgeNexus, ActiveTopics> nexusTopics = new ConcurrentHashMap<>();
  
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
    final long now = System.currentTimeMillis();
    for (Map.Entry<EdgeNexus, ActiveTopics> nexusTopicEntry : nexusTopics.entrySet()) {
      for (Map.Entry<String, ActiveTopic> activeTopicEntry : nexusTopicEntry.getValue().map.entrySet()) {
        final long allowed = activeTopicEntry.getValue().getAllowedMillis(now);
      }
    }
  }
  
  private ActiveTopic query(EdgeNexus nexus, String topic) {
    final ActiveTopics existingTopics = nexusTopics.get(nexus);
    return existingTopics != null ? existingTopics.map.get(topic) : null;
  }
  
  private ActiveTopic update(EdgeNexus nexus, String topic) {
    final ActiveTopics topics = Maps.putAtomic(nexusTopicsLock, nexusTopics, nexus, ActiveTopics::new);
    return Maps.putAtomic(topics, topics.map, topic, ActiveTopic::new);
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
    final ActiveTopic existing = query(nexus, topic);
    
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
          final ActiveTopic activeTopic = update(nexus, topic);
          activeTopic.expiryTime = millis != 0 ? now + millis : 0;
          outcome.allow(millis);
        }

        @Override
        public void deny(TopicAccessError error) {
          outcome.deny(error); 
        }
      });
    }
  }

  @Override
  public String toString() {
    return "CachedAuthenticator [scanIntervalMillis: " + scanIntervalMillis + ", delegate: " + delegate + "]";
  }
}
