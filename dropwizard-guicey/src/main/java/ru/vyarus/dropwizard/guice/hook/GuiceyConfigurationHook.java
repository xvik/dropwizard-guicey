package ru.vyarus.dropwizard.guice.hook;

import ru.vyarus.dropwizard.guice.GuiceBundle;

/**
 * Guicey configuration hook used to amend application configuration. Primarily intended to be used in tests with
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp} and
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp} (junit 5 or spock 2).
 * <p>
 * Hook could be registered with {@link #register()} method call.
 * <p>
 * Hooks are thread-scoped: it is assumed that registration thread is the same thread where application
 * will start.
 * <p>
 * Hooks could be enabled with a system property "guicey.hooks". Property value must contain comma-separated list of
 * complete hook class names. Each hook in list must have default no-args constructor. Example:
 * {@code -Dguicey.hooks=com.foo.MyHook1,com.foo.MyHook2}. Aliases may be assigned to simplify hooks enabling
 * {@link GuiceBundle.Builder#hookAlias(String, Class)}.
 * <p>
 * Enabling hooks from system property may be used for enabling reporting or additional tooling on already
 * compiled applications. For example, bundled {@link ru.vyarus.dropwizard.guice.debug.hook.DiagnosticHook} could
 * enable guicey diagnostic reports (enabled during development with print* methods on {@link GuiceBundle}) with
 * system property: {@code -Dguicey.hooks=diagnostic}.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
 * @see ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
 * @since 11.04.2018
 */
@FunctionalInterface
public interface GuiceyConfigurationHook {

    /**
     * Configuration is applied just after manual configuration (through bundle's builder in application class).
     * <p>
     * {@link GuiceBundle.Builder} contains special methods for test support:
     * <ul>
     * <li>Generic disable:
     * {@link GuiceBundle.Builder#disable(java.util.function.Predicate[])}
     * </li>
     * <li>Direct disable* method, for example
     * {@link GuiceBundle.Builder#disableExtensions(Class[])}</li>
     * <li>Guice bindings override:
     * {@link GuiceBundle.Builder#modulesOverride(com.google.inject.Module...)}</li>
     * </ul>
     * All other configuration options are also available, so it is possible to register extra extensions, bundles etc.
     * or modify guicey options ({@link GuiceBundle.Builder#option(Enum, Object)}).
     * <p>
     * All configuration items, registered with hook will be scoped as {@link GuiceyConfigurationHook}
     * instead of {@link io.dropwizard.core.Application} and so will be clearly distinguishable in configuration logs
     * ({@link GuiceBundle.Builder#printDiagnosticInfo()}).
     *
     * @param builder just created bundle's builder
     * @throws java.lang.Exception on error (simplify usage)
     */
    void configure(GuiceBundle.Builder builder) throws Exception;

    /**
     * Register hook. Note that it must be called before guicey bundle creation, otherwise will never be called.
     */
    default void register() {
        ConfigurationHooksSupport.register(this);
    }
}
