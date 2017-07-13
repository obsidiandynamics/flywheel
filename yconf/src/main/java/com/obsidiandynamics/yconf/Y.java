package com.obsidiandynamics.yconf;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

/**
 *  Explicitly provides a {@link YMapper}, specifying how to construct the annotated
 *  class from an {@link YObject} wrapping a DOM fragment.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Y {
  Class<? extends YMapper> value();
}
