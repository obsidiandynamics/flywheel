package au.com.williamhill.flywheel.edge.backplane;

final class TestBackplaneMessage {
  final String brokerId;
  final long messageId;
  
  TestBackplaneMessage(String brokerId, long messageId) {
    this.brokerId = brokerId;
    this.messageId = messageId;
  }

  @Override
  public String toString() {
    return brokerId + "-" + messageId;
  }
  
  static TestBackplaneMessage fromString(String str) {
    final String[] frags = str.split("-");
    return new TestBackplaneMessage(frags[0], Long.parseLong(frags[1]));
  }
}