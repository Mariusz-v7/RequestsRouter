package pl.mrugames.synapse.annotations;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.lang.annotation.*;

@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Validated
public @interface Controller {
    String value() default "";
}
