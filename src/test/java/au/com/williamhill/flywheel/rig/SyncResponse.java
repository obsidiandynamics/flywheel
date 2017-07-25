package au.com.williamhill.flywheel.rig;

final class SyncResponse extends RigSubframe {
  private final long nanoTime;

  SyncResponse(long nanoTime) {
    this.nanoTime = nanoTime;
  }
  
  long getNanoTime() {
    return nanoTime;
  }

  @Override
  public String toString() {
    return "SyncResponse [nanoTime=" + nanoTime + "]";
  }
}
