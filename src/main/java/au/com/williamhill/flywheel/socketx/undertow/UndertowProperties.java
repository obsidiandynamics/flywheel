package au.com.williamhill.flywheel.socketx.undertow;

import com.obsidiandynamics.indigo.util.*;

public final class UndertowProperties {
  static int ioThreads;
  
  static int coreTaskThreads;
  
  static int maxTaskThreads;
  
  static int bufferSize;
  
  static boolean directBuffers;
  
  static {
    final int cores = Runtime.getRuntime().availableProcessors();
    ioThreads = PropertyUtils.get("socketx.undertow.ioThreads", Integer::parseInt, Math.max(cores, 2));
    coreTaskThreads = PropertyUtils.get("socketx.undertow.coreTaskThreads", Integer::parseInt, ioThreads * 8);
    maxTaskThreads = PropertyUtils.get("socketx.undertow.maxTaskThreads", Integer::parseInt, coreTaskThreads);
    bufferSize = PropertyUtils.get("socketx.undertow.bufferSize", Integer::parseInt, 1024);
    directBuffers = PropertyUtils.get("socketx.undertow.directBuffers", Boolean::parseBoolean, false);
  }
}
