package ru.vyarus.dropwizard.guice.examples.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for method parameter.
 *
 * @author Vyacheslav Rusakov
 * @since 12.01.2018
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CustomValidator.class})
@Documented
public @interface CustomCondition {

    // could be localization key
    String message() default "Very specific case check failed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
