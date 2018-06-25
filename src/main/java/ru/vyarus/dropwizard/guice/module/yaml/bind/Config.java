package ru.vyarus.dropwizard.guice.module.yaml.bind;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Guice qualifier annotation for configuration values binding. Could be used to bind:
 * <ul>
 * <li>Configuration object itself by any class or interface: {@code @Inject @Config Configuration config}
 * (note that root configuration classes may be used without qualifier also for compatibility)</li>
 * <li>Unique configuration sub objects: {@code @Inject @Config SubConfiguration sub}</li>
 * <li>Configuration value by path (yaml): {@code @Inject @Config("property.path") String value}. Value type
 * must match property declaration type (including generics).</li>
 * </ul>
 * <p>
 * Generics are mostly useful for collection classes: {@code @Inject @Config("some.path) List<String> values;}.
 * Binding without generic is impossible (you must use all available type information).
 * <p>
 * Note that only properties visible for writing are present: properties which jackson could read. Even dropwizard
 * gude contains examples when configuration setter did not store value and use it immediately to create some other
 * objects. Obviously such properties are impossible to "read back" and so impossible to bind.
 * <p>
 * Property visibility may also be affected by annotated getter absence: for example, suppose there are no getters and
 * {@code @JsonProperty private String foo}, {@code private String bar} then only annotated "foo" property would be
 * visible. When property getter is present - property will be found, even without annotations.
 *
 * @author Vyacheslav Rusakov
 * @see ConfigBindingModule for available bindings
 * @since 04.05.2018
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface Config {

    /**
     * @return configuration path (may be empty for internal unique configuration objects or root classes)
     */
    String value() default "";
}
