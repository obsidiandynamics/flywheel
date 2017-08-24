package au.com.williamhill.flywheel.edge.auth;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.slf4j.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.util.*;

public final class CachedAuthenticator extends Thread implements Authenticator {
  private static final Logger LOG = LoggerFactory.getLogger(CachedAuthenticator.class);
  
  private static final class ActiveTopics {
    final Map<String, ActiveTopic> map = new ConcurrentHashMap<>();
  }
  
  private static final class ActiveTopic {
    long expiryTime;
    
    long lastQueriedTime;

    long getRemainingMillis(long now) {
      return expiryTime == 0 ? Long.MAX_VALUE : expiryTime - now;
    }
    
    long getQueriedAgo(long now) {
      return now - lastQueriedTime;
    }
  }
  
  private final Map<EdgeNexus, ActiveTopics> nexusTopics = new ConcurrentHashMap<>();
  
  private final Object nexusTopicsLock = new Object();
  
  private final CachedAuthenticatorConfig config;
  
  private final NestedAuthenticator delegate;
  
  private final AtomicInteger pendingQueries = new AtomicInteger();
  
  private AuthConnector connector;
  
  private volatile boolean running = true;
  
  public CachedAuthenticator(CachedAuthenticatorConfig config, NestedAuthenticator delegate) {
    super(String.format("CachedAuthenticatorWatchdog[runInterval=%dms]", config.runIntervalMillis));
    this.config = config;
    this.delegate = delegate;
  }
  
  @Override
  public void run() {
    while (running) {
      cycle();
      try {
        Thread.sleep(config.runIntervalMillis);
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
        if (pendingQueries.get() >= config.maxPendingQueries) return;
        
        final ActiveTopic activeTopic = activeTopicEntry.getValue();
        final long remaining = activeTopic.getRemainingMillis(now);
        if (remaining < config.queryBeforeExpiryMillis) {
          final long queriedAgo = activeTopic.getQueriedAgo(now);
          if (queriedAgo > config.minQueryIntervalMillis) {
            if (LOG.isTraceEnabled()) LOG.trace("{}: {} ms remaining; querying delegate", nexusTopicEntry.getKey(), remaining);
            query(nexusTopicEntry.getKey(), activeTopicEntry.getKey(), nexusTopicEntry.getValue(), activeTopic);
          }
        }
      }
    }
  }
  
  private void query(EdgeNexus nexus, String topic, ActiveTopics activeTopics, ActiveTopic activeTopic) {
    final long now = System.currentTimeMillis();
    activeTopic.lastQueriedTime = now;
    pendingQueries.incrementAndGet();
    delegate.verify(nexus, topic, new AuthenticationOutcome() {
      @Override
      public void allow(long millis) {
        pendingQueries.decrementAndGet();
        if (LOG.isTraceEnabled()) LOG.trace("{}: allowed for {} ms", nexus, millis);
        activeTopic.expiryTime = millis != 0 ? now + millis : 0;
      }

      @Override
      public void deny(TopicAccessError error) {
        pendingQueries.decrementAndGet();
        activeTopics.map.remove(topic);
        if (LOG.isTraceEnabled()) LOG.trace("{}: denied with {}", nexus, error);
        connector.expireTopic(nexus, topic);
      }
    });
  }
  
  private ActiveTopic get(EdgeNexus nexus, String topic) {
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
    final ActiveTopic existing = get(nexus, topic);
    
    final long cachedRemainingMillis = existing != null ? existing.getRemainingMillis(now) : -1;
    
    if (cachedRemainingMillis > 0) {
      // was cached, and the cached entry is still allowed
      outcome.allow(cachedRemainingMillis == Long.MAX_VALUE ? AuthenticationOutcome.INDEFINITE : cachedRemainingMillis);
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
    return "CachedAuthenticator [config: " + config + ", delegate: " + delegate + "]";
  }
}
