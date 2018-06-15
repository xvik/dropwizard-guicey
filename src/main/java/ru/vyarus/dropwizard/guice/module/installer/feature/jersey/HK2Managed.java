package ru.vyarus.dropwizard.guice.module.installer.feature.jersey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate jersey extension to delegate bean creation into HK2.
 * <p>
 * Works for extensions registered by
 * {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller}.
 * <p>
 * Guice context is started before HK2, but HK2 related bindings (using service locator instance) will appear
 * in guice context only after HK2 context creation. So if bean directly depends on HK2 services
 * (dependencies can't be wrapped with {@link javax.inject.Provider}, there is no way to properly create
 * it in guice context.
 * <p>
 * Good examples for this are {@link org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider}
 * and {@link org.glassfish.jersey.server.internal.inject.ParamInjectionResolver}. Both are required to implement
 * new parameter annotation and both will start immediately in HK2 context.
 * <p>
 * Still guice bindings could be used in HK2 managed bean (especially other extensions, installed by
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller}.
 * In case of problems with lifecycle, simply use {@link javax.inject.Provider} to wrap actual binding and
 * delay it's resolution.
 * <p>
 * In fact, using this annotation is the same as registering bean directly in jersey. Installer just
 * simplifies binder definition to simple annotation.
 * <p>
 * Annotation will do nothing if HK2-first mode enabled:
 * {@link ru.vyarus.dropwizard.guice.module.installer.InstallersOptions#JerseyExtensionsManagedByGuice}
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding as alternative solution
 * @see GuiceManaged
 * @since 21.11.2014
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HK2Managed {
}
