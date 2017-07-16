package au.com.williamhill.flywheel;

import org.slf4j.*;

import com.obsidiandynamics.yconf.*;

import au.com.williamhill.flywheel.edge.*;
import au.com.williamhill.flywheel.frame.*;

@Y
public final class ProfileLauncher implements Launcher, TopicListener  {
  private static final Logger LOG = LoggerFactory.getLogger(ProfileLauncher.class);

  @Override
  public void launch(String[] args, Profile profile) throws Exception {
    final EdgeNode edge = EdgeNode.builder()
        .withServerConfig(profile.serverConfig)
        .withBackplane(profile.backplane)
        .build();
    edge.addTopicListener(this);
  }
  
  @Override
  public void onOpen(EdgeNexus nexus) {
    LOG.info("{}: opened", nexus);
  }

  @Override
  public void onClose(EdgeNexus nexus) {
    LOG.info("{}: closed", nexus);
  }

  @Override
  public void onBind(EdgeNexus nexus, BindFrame bind, BindResponseFrame bindRes) {
    LOG.info("{}: bind {} -> {}", nexus, bind, bindRes);
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishTextFrame pub) {
    LOG.info("{}: publish {}", nexus, pub);
  }

  @Override
  public void onPublish(EdgeNexus nexus, PublishBinaryFrame pub) {
    LOG.info("{}: publish {}", nexus, pub);
  }
}