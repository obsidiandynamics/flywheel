package com.obsidiandynamics.yconf;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

/**
 *  Instruction to reflectively assign an attribute value or a constructor
 *  parameter.
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface YInject {
  String name() default "";
  Class<?> type() default Void.class;
}
