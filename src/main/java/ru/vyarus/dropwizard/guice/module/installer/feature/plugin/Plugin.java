package ru.vyarus.dropwizard.guice.module.installer.feature.plugin;

import java.lang.annotation.*;

/**
 * Simplifies work with guice multibindings.
 * Annotate one or more beans with extension point declaration (interface) and
 * you then can autowire collection of found plugins.
 * Internally each bean is registered into {@link com.google.inject.multibindings.Multibinder}.
 * <p>To use {@link com.google.inject.multibindings.MapBinder} create your own annotation with single
 * attribute {@code value} and annotate it with {@code @Plugin}. New annotation value will be used as key.</p>
 * or  if name set.
 *
 * @author Vyacheslav Rusakov
 * @since 08.10.2014
 */
@Target({
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Plugin {

    /**
     * @return Plugin type.
     */
    Class<?> value();
}
