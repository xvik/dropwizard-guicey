package ru.vyarus.dropwizard.guice.module.support.scope;

import com.google.inject.ScopeAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Guice prototype scope annotation. Could be used to annotate jersey extensions to prevent forced singleton scope.
 *
 * @author Vyacheslav Rusakov
 * @since 04.05.2018
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface Prototype {
}
