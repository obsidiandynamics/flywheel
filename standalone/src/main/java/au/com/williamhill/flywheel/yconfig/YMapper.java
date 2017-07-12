package au.com.williamhill.flywheel.yconfig;

/**
 *  Specifies how an instance of a specific class can be created from a given Yaml fragment.
 *
 *  @param <F> The Yaml fragment type. (E.g. Map<?, ?>, List<?>, or a scalar.)
 *  @param <T> The class type.
 */
@FunctionalInterface
public interface YMapper<F, T> {
  T map(F yaml, YContext context);
}
