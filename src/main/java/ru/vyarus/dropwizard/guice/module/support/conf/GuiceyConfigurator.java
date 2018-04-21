package ru.vyarus.dropwizard.guice.module.support.conf;

import ru.vyarus.dropwizard.guice.GuiceBundle;

/**
 * Configurator used to amend application configuration. Primarily intended to be used in tests with
 * {@link ru.vyarus.dropwizard.guice.test.GuiceyConfiguratorRule} (junit) and
 * {@link ru.vyarus.dropwizard.guice.test.spock.UseGuiceyConfigurator} (spock).
 * <p>
 * Also could be used for {@link ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener} (listener,
 * implementing configurator interface is recognized and registered automatically).
 * <p>
 * Configurators are thread-scoped: it is assumed that registration thread is the same thread where application
 * will start.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.test.GuiceyConfiguratorRule
 * @see ru.vyarus.dropwizard.guice.test.spock.UseGuiceyConfigurator
 * @since 11.04.2018
 */
@FunctionalInterface
public interface GuiceyConfigurator {

    /**
     * Configuration is applied just after manual configuration (through bundle's builder in application class).
     * <p>
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder} contains special methods for test support:
     * <ul>
     * <li>Generic disable:
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disable(java.util.function.Predicate[])}
     * </li>
     * <li>Direct disable* method, for example
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableExtensions(Class[])}</li>
     * <li>Guice bindings override:
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#overrideModules(com.google.inject.Module...)}</li>
     * </ul>
     * All other configuration options are also available, so it is possible to register extra extensions, bundles etc
     * or modify guicey options ({@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#option(Enum, Object)}).
     * <p>
     * All configuration items, registered with configurator will be scoped as {@link GuiceyConfigurator}
     * instead of {@link io.dropwizard.Application} and so will be clearly distinguishable in configuration logs
     * ({@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#printDiagnosticInfo()}).
     *
     * @param builder just created bundle's builder
     * @see ru.vyarus.dropwizard.guice.test.GuiceyConfiguratorRule for more information
     */
    void configure(GuiceBundle.Builder builder);
}
