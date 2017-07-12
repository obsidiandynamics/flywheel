package au.com.williamhill.flywheel.yconfig;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

/**
 *  Explicitly provides a {@link YMapper}, specifying how to construct the annotated
 *  class from a Yaml fragment.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Y {
  Class<? extends YMapper<?, ?>> value();
}
