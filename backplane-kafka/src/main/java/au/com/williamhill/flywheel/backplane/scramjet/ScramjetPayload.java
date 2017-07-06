package au.com.williamhill.flywheel.backplane.scramjet;

@FunctionalInterface
public interface ScramjetPayload {
  AttributeWriter pack();
}
