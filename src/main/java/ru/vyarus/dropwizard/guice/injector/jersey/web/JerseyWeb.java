package ru.vyarus.dropwizard.guice.injector.jersey.web;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Guice {@link com.google.inject.servlet.ServletModule} provide it's own request and response bindings.
 * But original jersey request, response and servlet context are still bound with this qualifier (just in case).
 *
 * @author Vyacheslav Rusakov
 * @since 02.05.2019
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface JerseyWeb {
}
