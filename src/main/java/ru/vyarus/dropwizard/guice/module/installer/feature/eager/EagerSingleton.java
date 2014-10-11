package ru.vyarus.dropwizard.guice.module.installer.feature.eager;

import java.lang.annotation.*;

/**
 * Annotated beans will be injected in guice module as singleton.
 * (equivalent of bind(type).asEagerSingleton).
 * Ideal for any kind of initializers (e.g. with @PostConstruct)
 * <p>NOTE: use sparingly, because such initialization logic contradicts with guice ideology
 * (such initializer may be suitable for quick prototype or some rare exceptional cases).</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EagerSingleton {
}
