package au.com.williamhill.flywheel.topic;

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
  
  public static TopicSpec smallLeaves() {
    return TopicSpec.builder()
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(1, 0, 0).nodes(5))
        .build();
  }
  
  public static TopicSpec mediumLeaves() {
    return TopicSpec.builder()
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(1, 0, 0).nodes(5))
        .build();
  }
  
  public static TopicSpec largeLeaves() {
    return TopicSpec.builder()
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(1, 0, 0).nodes(5))
        .build();
  }
  
  public static TopicSpec jumboLeaves() {
    return TopicSpec.builder()
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(1, 0, 0).nodes(5))
        .build();
  }
  
  public static TopicSpec jumboLeaves2() {
    return TopicSpec.builder()
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(4))
        .add(new NodeSpec(1, 0, 0).nodes(5))
        .build();
  }
  
  public static TopicSpec jumboLeaves3() {
    return TopicSpec.builder()
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(6))
        .add(new NodeSpec(1, 0, 0).nodes(5))
        .build();
  }
  
  public static TopicSpec jumboLeaves4() {
    return TopicSpec.builder()
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(8))
        .add(new NodeSpec(1, 0, 0).nodes(5))
        .build();
  }
  
  public static TopicSpec jumboLeaves5() {
    return TopicSpec.builder()
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(2))
        .add(new NodeSpec(0, 0, 0).nodes(5))
        .add(new NodeSpec(0, 0, 0).nodes(10))
        .add(new NodeSpec(1, 0, 0).nodes(5))
        .build();
  }
  
  public static TopicSpec tiny() {
    return TopicSpec.builder()
        .add(new NodeSpec(1, 1, 1).nodes(1))
        .build();
  }
  
  public static TopicSpec small() {
    return TopicSpec.builder()
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .build();
  }
  
  public static TopicSpec medium() {
    return TopicSpec.builder()
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .build();
  }
  
  public static TopicSpec large() {
    return TopicSpec.builder()
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .build();
  }
  
  public static TopicSpec jumbo() {
    return TopicSpec.builder()
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .build();
  }
  
  public static TopicSpec mriya() {
    return TopicSpec.builder()
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .add(new NodeSpec(1, 1, 1).nodes(2))
        .add(new NodeSpec(1, 1, 1).nodes(5))
        .build();
  }
}
