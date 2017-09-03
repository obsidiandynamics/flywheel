package au.com.williamhill.flywheel.socketx.netty;

import au.com.williamhill.flywheel.socketx.attribute.Attribute;
import au.com.williamhill.flywheel.socketx.attribute.Constant;
import io.netty.util.*;

public final class NettyAtts {
  public static final Attribute<Integer> EVENT_LOOP_THREADS = new Attribute<Integer>("socketx.netty.eventLoopThreads")
      .withMin(Constant.of(1))
      .withDefault(Constant.of(NettyRuntime.availableProcessors() * 2));
  
  private NettyAtts() {}
}
