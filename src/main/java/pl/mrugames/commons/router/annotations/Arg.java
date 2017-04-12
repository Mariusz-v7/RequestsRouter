package pl.mrugames.commons.router.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Arg {
    String value();

    String defaultValue() default ArgDefaultValue.ARG_DEFAULT_VALUE;
}
