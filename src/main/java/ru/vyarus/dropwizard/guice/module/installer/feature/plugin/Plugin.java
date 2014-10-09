package ru.vyarus.dropwizard.guice.module.installer.feature.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simplifies work with guice multibindings.
 * Annotate one or more beans with extension point declaration (interface) and
 * you then can autowire collection of found plugins.
 * Internally each bean is registered into multibinder.
 *
 * @author Vyacheslav Rusakov
 * @since 08.10.2014
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Plugin {

    /**
     * @return Plugin type.
     */
    Class<?> value();
}
