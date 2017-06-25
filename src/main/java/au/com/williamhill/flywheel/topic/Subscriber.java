package au.com.williamhill.flywheel.topic;

@FunctionalInterface
public interface Subscriber {
  void accept(Delivery delivery);
}
