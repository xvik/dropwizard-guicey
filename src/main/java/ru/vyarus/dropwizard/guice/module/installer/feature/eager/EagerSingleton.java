package ru.vyarus.dropwizard.guice.module.installer.feature.eager;

import java.lang.annotation.*;

/**
 * Annotated beans will be injected in guice module as singleton.
 * (equivalent of {@code bind(type).asEagerSingleton()}).
 * Ideal for any kind of initializers (e.g. with @PostConstruct)
 * <p>
 * If annotated extension is manually bound in guice then extension will simply check that bean is bound
 * as eager singleton and fail otherwise (because it is not possible to modify existing binding).
 * <p>
 * NOTE: use sparingly, because such initialization logic contradicts with guice ideology
 * (such initializer may be suitable for quick prototype or some rare exceptional cases).
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EagerSingleton {
}
