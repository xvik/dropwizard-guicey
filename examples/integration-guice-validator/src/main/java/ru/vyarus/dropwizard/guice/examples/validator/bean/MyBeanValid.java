package ru.vyarus.dropwizard.guice.examples.validator.bean;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation to validate {@link MyBean} bean type. Will be used on bean directly, but it could be used
 * as validation annotation for method too (when not declared on bean).
 *
 * @author Vyacheslav Rusakov
 * @since 12.01.2018
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {MyBeanValidator.class})
@Documented
public @interface MyBeanValid {

    // could be localization key
    String message() default "Bean is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
