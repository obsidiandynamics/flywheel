package au.com.williamhill.flywheel.edge.plugin.toplog;

import java.util.*;

import org.slf4j.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.edge.plugin.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.topic.*;

@Y
public final class TopicLogger implements Plugin, TopicListener {
  private Logger logger = LoggerFactory.getLogger(TopicLogger.class);
  
  @YInject
  private Topic[] excludeTopics = new Topic[0];
  
  public TopicLogger withLogger(Logger logger) {
    this.logger = logger;
    return this;
  }
  
  public TopicLogger withExcludeTopics(Topic... excludeTopics) {
    this.excludeTopics = excludeTopics;
    return this;
  }
  
  @Override
  public void onBuild(EdgeNodeBuilder builder) throws Exception {}
  
  @Override
  public void onRun(EdgeNode edge) throws Exception {
    edge.addTopicListener(this);
  }

  @Override
  public void close() {}
  
  @Override
  public void onOpen(EdgeNexus nexus) {
    if (logger.isInfoEnabled()) logger.info("{}: opened", nexus);
  }

  @Override
  public void onClose(EdgeNexus nexus) {
    if (logger.isInfoEnabled()) logger.info("{}: closed", nexus);
  }

  @Override
  public void onBind(EdgeNexus nexus, BindFrame bind, BindResponseFrame bindRes) {
    if (logger.isDebugEnabled()) logger.debug("{}: bind {} -> {}", nexus, bind, bindRes);
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {
    if (shouldLog(pub.getTopic())) logger.debug("{}: publish {}", nexus, pub);
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
    if (shouldLog(pub.getTopic())) logger.debug("{}: publish {}", nexus, pub);
  }
  
  private boolean shouldLog(String topic) {
    if (! logger.isDebugEnabled()) return false;
    if (excludeTopics.length == 0) return true;
    
    final Topic published = Topic.of(topic);
    for (Topic excludeTopic : excludeTopics) {
      if (excludeTopic.accepts(published)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return "TopicLogger [exclude topics: " + Arrays.toString(excludeTopics) + "]";
  }
}
