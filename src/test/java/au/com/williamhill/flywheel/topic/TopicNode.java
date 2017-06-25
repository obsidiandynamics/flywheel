package au.com.williamhill.flywheel.topic;

final class TopicNode {
  final String fragment;
  final NodeSpec spec;
  
  TopicNode(String fragment, NodeSpec spec) {
    this.fragment = fragment;
    this.spec = spec;
  }

  @Override
  public String toString() {
    return "TopicNode [fragment=" + fragment + ", spec=" + spec + "]";
  }
}
