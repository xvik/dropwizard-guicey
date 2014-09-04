package ru.vyarus.dropwizard.guice.module.installer.scanner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to exclude class from classpath scanning.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InvisibleForScanner {
}
