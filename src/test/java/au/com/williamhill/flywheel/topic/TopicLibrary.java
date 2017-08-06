package au.com.williamhill.flywheel.topic;

import java.io.*;
import java.net.*;

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
  
  public static TopicSpec load(String uri) throws FileNotFoundException, IOException, URISyntaxException {
    return new MappingContext()
        .withParser(new SnakeyamlParser())
        .fromStream(getStream(new URI(uri)))
        .map(TopicSpec.class);
  }
  
  private static InputStream getStream(URI uri) throws FileNotFoundException {
    switch (uri.getScheme()) {
      case "file":
        return new FileInputStream(new File(uri.getHost() + uri.getPath()));
        
      case "cp":
      case "classpath":
        return TopicLibrary.class.getClassLoader().getResourceAsStream(uri.getHost() + uri.getPath());
        
      default:
        throw new IllegalArgumentException("Unsupported URI scheme " + uri.getScheme());
    }
  }
}
