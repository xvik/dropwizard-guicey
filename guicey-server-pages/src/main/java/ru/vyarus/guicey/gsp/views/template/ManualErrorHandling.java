package ru.vyarus.guicey.gsp.views.template;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * By default, GSP errors mechanism intercept errors before {@link jakarta.ws.rs.ext.ExceptionMapper} and
 * {@link io.dropwizard.jersey.errors.ErrorEntityWriter} and so their result is ignored. For the majority of cases
 * this is acceptable behaviour because it grants custom error pages. But in some cases, it may be required
 * to use standard mechanisms instead.
 * <p>
 * This marker annotation could be used on resource method or entire resource class (to affect all methods) to
 * disable GSP errors mechanism.
 *
 * @author Vyacheslav Rusakov
 * @since 10.06.2019
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface ManualErrorHandling {
}
