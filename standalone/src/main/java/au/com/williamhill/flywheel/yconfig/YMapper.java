package au.com.williamhill.flywheel.yconfig;

/**
 *  Specifies how an instance of a specific class can be created from a given Yaml fragment.
 */
@FunctionalInterface
public interface YMapper {
  Object map(YObject y);
}
