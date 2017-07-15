package com.obsidiandynamics.yconf;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

/**
 *  Explicitly provides a {@link TypeMapper}, specifying how to construct the annotated
 *  class from an {@link YObject} wrapping a DOM fragment.<p>
 *  
 *  If a {@link TypeMapper} class is unspecified, the {@code ReflectiveMapper} will
 *  be used by default.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Y {
  Class<? extends TypeMapper> value() default ReflectiveMapper.class;
}
