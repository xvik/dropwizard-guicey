package ru.vyarus.dropwizard.guice.hook;

import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.GuiceyHooksRule;
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyHooks;

/**
 * Guicey configuration hook used to amend application configuration. Primarily intended to be used in tests with
 * {@link GuiceyHooksRule} (junit) and
 * {@link UseGuiceyHooks} (spock).
 * <p>
 * Hook could be registered with {@link #register()} method call.
 * <p>
 * Also, could be used with {@link ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener} (listener,
 * implementing hook interface is recognized and registered automatically).
 * <p>
 * Hooks are thread-scoped: it is assumed that registration thread is the same thread where application
 * will start.
 *
 * @author Vyacheslav Rusakov
 * @see GuiceyHooksRule
 * @see UseGuiceyHooks
 * @since 11.04.2018
 */
@FunctionalInterface
public interface GuiceyConfigurationHook {

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
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modulesOverride(com.google.inject.Module...)}</li>
     * </ul>
     * All other configuration options are also available, so it is possible to register extra extensions, bundles etc
     * or modify guicey options ({@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#option(Enum, Object)}).
     * <p>
     * All configuration items, registered with hook will be scoped as {@link GuiceyConfigurationHook}
     * instead of {@link io.dropwizard.Application} and so will be clearly distinguishable in configuration logs
     * ({@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#printDiagnosticInfo()}).
     *
     * @param builder just created bundle's builder
     * @see GuiceyHooksRule for more information
     */
    void configure(GuiceBundle.Builder builder);

    /**
     * Register hook. Note that it must be called before guicey bundle creation, otherwise will never be called.
     */
    default void register() {
        ConfigurationHooksSupport.register(this);
    }
}
