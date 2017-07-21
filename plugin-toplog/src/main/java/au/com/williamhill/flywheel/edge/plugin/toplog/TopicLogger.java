package au.com.williamhill.flywheel.edge.plugin.toplog;

import java.util.*;

import org.slf4j.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;
import au.com.williamhill.flywheel.topic.*;

@Y
public final class TopicLogger implements Plugin, TopicListener {
  private static final Logger LOG = LoggerFactory.getLogger(TopicLogger.class);
  
  @YInject
  private Topic[] logExcludeTopics = new Topic[0];
  
  @Override
  public void onBuild(EdgeNodeBuilder builder) throws Exception {}
  
  @Override
  public void onRun(EdgeNode edge) throws Exception {
    edge.addTopicListener(this);
  }

  @Override
  public void close() throws Exception {}
  
  @Override
  public void onOpen(EdgeNexus nexus) {
    if (LOG.isInfoEnabled()) LOG.info("{}: opened", nexus);
  }

  @Override
  public void onClose(EdgeNexus nexus) {
    if (LOG.isInfoEnabled()) LOG.info("{}: closed", nexus);
  }

  @Override
  public void onBind(EdgeNexus nexus, BindFrame bind, BindResponseFrame bindRes) {
    if (LOG.isDebugEnabled()) LOG.debug("{}: bind {} -> {}", nexus, bind, bindRes);
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {
    if (LOG.isDebugEnabled() && shouldLog(pub.getTopic())) LOG.debug("{}: publish {}", nexus, pub);
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
    if (LOG.isDebugEnabled() && shouldLog(pub.getTopic())) LOG.debug("{}: publish {}", nexus, pub);
  }
  
  private boolean shouldLog(String topic) {
    if (logExcludeTopics.length == 0) return true;
    
    final Topic published = Topic.of(topic);
    for (Topic logExcludeTopic : logExcludeTopics) {
      if (logExcludeTopic.accepts(published)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return "TopicLogger {exclude topics: " + Arrays.toString(logExcludeTopics) + "}";
  }
}
