package au.com.williamhill.flywheel.topic;

import java.io.*;
import java.net.*;

import com.obsidiandynamics.socketx.util.*;
import com.obsidiandynamics.yconf.*;

public final class TopicLibrary {
  private TopicLibrary() {}
  
  public static TopicSpec singleton(int subscribers) {
    return TopicSpec.builder()
        .add(new NodeSpec(subscribers, 0, 0).nodes(1))
        .build();
  }
  
  public static TopicSpec shrub(int leaves) {
    return TopicSpec.builder()
        .add(new NodeSpec(1, 0, 0).nodes(leaves))
        .build();
  }
  
  public static TopicSpec load(String location) throws IOException, URISyntaxException {
    return new MappingContext()
        .withParser(new SnakeyamlParser())
        .fromStream(ResourceLocator.asStream(new URI(location)))
        .map(TopicSpec.class);
  }
}
