package ru.vyarus.dropwizard.guice.module.installer.feature.eager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated beans will be injected in guice module, even if no other service initialize it.
 * Ideal for any kind of initializers (e.g. with @PostConstruct)
 * <p>NOTE: use sparingly, because such initialization logic contradicts with guice ideology
 * (such initializer may be suitable for quick prototype or some rare exceptional cases).</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Eager {
}
