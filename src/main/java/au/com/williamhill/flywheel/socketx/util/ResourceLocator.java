package au.com.williamhill.flywheel.socketx.util;

import java.io.*;
import java.net.*;

import au.com.williamhill.flywheel.topic.*;

public final class ResourceLocator {
  public static InputStream asStream(URI uri) throws FileNotFoundException {
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
