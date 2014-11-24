package ru.vyarus.dropwizard.guice.module.installer.install.binding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By default, all resolved extensions are registered in injector by {@code binder.bind(extType)}.
 * If extension installer implements
 * {@link ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller} then installer manually
 * registers extension in context.
 * <p>Registering bean in context means that all singleton services will be instantiated on context start
 * (for production mode) </p>
 * <p>{@code @LazyBinding} allows omitting registration in simple case and will provide lazy hint for installer
 * with specific binding. Such lazy bean may be useful to defer bean creation: e.g. it is used somewhere as
 * dependency, but wrapped in {@code Provider}, so actual instance is not created immediately.</p>
 * <p>The best example for lazy beans is guice managed jersey extensions: due to the way jersey is integrated,
 * for some time guice context is not aware of HK beans, and the opposite. By making bean lazy, it's creation
 * moved from guice context start to actual usage by HK or jersey, when it can be created.</p>
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller
 * @see ru.vyarus.dropwizard.guice.module.jersey.GuiceFeature
 * @see ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed as alternative for jersey extensions
 * @since 21.11.2014
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LazyBinding {
}
