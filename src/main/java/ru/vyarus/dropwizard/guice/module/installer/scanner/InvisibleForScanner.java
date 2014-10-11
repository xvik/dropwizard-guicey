package ru.vyarus.dropwizard.guice.module.installer.scanner;

import java.lang.annotation.*;

/**
 * Used to exclude class from classpath scanning.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface InvisibleForScanner {
}
