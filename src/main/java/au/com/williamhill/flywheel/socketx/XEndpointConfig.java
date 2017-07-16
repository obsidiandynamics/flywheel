package au.com.williamhill.flywheel.socketx;

import com.obsidiandynamics.yconf.*;

@Y
public class XEndpointConfig {
  @YInject
  public long highWaterMark = Long.MAX_VALUE;
  
  public XEndpointConfig withHighWaterMark(long highWaterMark) {
    this.highWaterMark = highWaterMark;
    return this;
  }

  @Override
  public String toString() {
    return "XEndpointConfig [highWaterMark=" + highWaterMark + "]";
  }
}
