package ru.vyarus.dropwizard.guice.module.installer.bundle;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.option.Option;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigTreeBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Guicey configuration object. Provides almost the same configuration methods as
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder}. Also, contains dropwizard configuration,
 * environment and bootstrap objects (in case if they are required).
 * <p>
 * In contrast to main builder, guicey bundle can't:
 * <ul>
 * <li>Disable bundles (because at this stage bundles already partly processed)</li>
 * <li>Use generic disable predicates (to not allow bundles disable, moreover it's tests-oriented feature)</li>
 * <li>Change options (because some bundles may already apply configuration based on changed option value
 * which will mean inconsistent state)</li>
 * <li>Register listener, implementing {@link ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook}
 * (because it's too late - all hooks were processed)</li>
 * <li>Register some special objects like custom injector factory or custrom bundles lookup</li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 01.08.2015
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class GuiceyBootstrap {

    private final ConfigurationContext context;
    private final List<GuiceyBundle> iterationBundles;

    public GuiceyBootstrap(final ConfigurationContext context, final List<GuiceyBundle> iterationBundles) {
        this.context = context;
        this.iterationBundles = iterationBundles;
    }

    /**
     * Note: application is already in run phase, so it's too late to configure dropwizard bootstrap object. Object
     * provided just for consultation.
     *
     * @return dropwizard bootstrap instance
     */
    public Bootstrap bootstrap() {
        return context.getBootstrap();
    }

    /**
     * @param <T> configuration type
     * @return configuration instance
     */
    @SuppressWarnings("unchecked")
    public <T extends Configuration> T configuration() {
        return (T) context.getConfiguration();
    }

    /**
     * May be used to access current configuration value by exact path. This is helpful for bundles universality:
     * suppose bundle X requires configuration object XConf, which is configured somewhere inside application
     * configuration. We can require configuration path in bundle constructor and use it to access required
     * configuration object: {@code new X("sub.config.path")}.
     *
     * @param yamlPath target value yaml path
     * @param <T> value type
     * @return configuration value by path or null if value is null or path not exists
     * @see #configurationTree() for custom configuration searches
     */
    public <T> T configuration(final String yamlPath) {
        return configurationTree().valueByPath(yamlPath);
    }

    /**
     * May be used to access unique sub configuration object. This is helpful for bundles universality:
     * suppose bundle X requires configuration object XConf and we are sure that only one declaration of XConf would
     * be used in target configuration class, then we can simply request it:
     * {@code configuration(XConf.class) == <instance of XConf or null>}.
     * <p>
     * Note that uniqueness is checked by declaration class:
     * <pre>{@code class Config extends Configuration {
     *     Sub sub;
     *     SubExt ext; // SubExt extends Sub
     * }}</pre>
     * are unique declarations (declaration of the same type never appears in configuration on any level).
     * {@code configuration(Sub.class) == sub} and {@code configuration(SubExt.class) == ext}.
     * <p>
     * Example of accessing server config from dropwizard configuration:
     * {@code configuration(ServerFactory.class) == DefaultServerFactory (or SimpleServerFactory)}
     * (see dropwizard {@link Configuration} class).
     *
     * @param type target configuration declaration type
     * @param <T> declaration type
     * @param <K> required value type (may be the same or extending type)
     * @return unique configuration value or null if value is null or no declaration found
     * @see #configurationTree() for custom configuration searches
     */
    public <T, K extends T> K configuration(final Class<T> type) {
        return configurationTree().valueByUniqueDeclaredType(type);
    }

    /**
     * IMPORTANT: method semantic is different from {@link #configuration(Class)}, which use direct class
     * declaration match, whereas this method searches by all assignable types.
     * <pre>{@code class Config extends Configuration {
     *     Sub sub;
     *     SubExt ext; // SubExt extends Sub
     * }}</pre>
     * {@code configurations(Sub.class) == [sub, ext]}, but {@code configurations(SubExt.class) == [ext]}.
     * <p>
     * Useful when multiple sub configuration objects could be used and all of them are required in some
     * universal bundle.
     * <p>
     * Note: only custom types may be used (sub configuration objects), not Integer, Boolean, List, etc.
     *
     * @param type target configuration type
     * @param <T> value type
     * @return list of configuration values with required type or empty list
     * @see #configurationTree() for custom configuration searches
     */
    public <T> List<? extends T> configurations(final Class<T> type) {
        return configurationTree().valuesByType(type);
    }

    /**
     * Raw configuration introspection info. Could be used for more sophisticated configuration searches then
     * provided in shortcut methods.
     * <p>
     * Note that configuration is analyzed using jackson serialization api, so not all configured properties
     * could be visible (when property getter is not exists or field not annotated).
     * <p>
     * Returned object contains all resolved configuration paths. Any path element could be traversed like a tree.
     * See find* and value* methods as an examples of how stored paths could be traversed.
     *
     * @return detailed configuration object
     * @see ConfigTreeBuilder for configuration introspection details
     * @see ru.vyarus.dropwizard.guice.module.yaml.bind.Config for available guice configuration bindings
     */
    public ConfigurationTree configurationTree() {
        return context.getConfigurationTree();
    }

    /**
     * @return environment instance
     */
    public Environment environment() {
        return context.getEnvironment();
    }

    /**
     * Application instance may be useful for complex (half manual) integrations where access for
     * injector is required.
     * For example, manually registered
     * {@link io.dropwizard.lifecycle.Managed} may access injector in it's start method by calling
     * {@link ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup#getInjector(Application)}.
     * <p>
     * NOTE: it will work in this example, because injector access will be after injector creation.
     * Directly inside bundle initialization method injector could not be obtained as it's not exists yet.
     *
     * @return dropwizard application instance
     */
    public Application application() {
        return context.getBootstrap().getApplication();
    }

    /**
     * Read option value. Options could be set only in application root
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#option(Enum, Object)}.
     * If value wasn't set there then default value will be returned. Null may return only if it was default value
     * and no new value were assigned.
     * <p>
     * Option access is tracked as option usage (all tracked data is available through
     * {@link ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo}).
     *
     * @param option option enum
     * @param <V>    option value type
     * @param <T>    helper type to define option
     * @return assigned option value or default value
     * @see Option more options info
     * @see ru.vyarus.dropwizard.guice.GuiceyOptions options example
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#option(java.lang.Enum, java.lang.Object)
     * options definition
     */
    public <V, T extends Enum & Option> V option(final T option) {
        return context.option(option);
    }

    /**
     * Register guice modules. All registered modules must be of unique type (duplicate instances of the
     * same type are filtered).
     *
     * @param modules one or more juice modules
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modules(com.google.inject.Module...)
     */
    public GuiceyBootstrap modules(final Module... modules) {
        Preconditions.checkState(modules.length > 0, "Specify at least one module");
        context.registerModules(modules);
        return this;
    }

    /**
     * Override modules (using guice {@link com.google.inject.util.Modules#override(Module...)}).
     *
     * @param modules overriding modules
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modulesOverride(Module...)
     */
    public GuiceyBootstrap modulesOverride(final Module... modules) {
        context.registerModulesOverride(modules);
        return this;
    }

    /**
     * If bundle provides new installers then they must be declared here.
     * Optionally, core or other 3rd party installers may be declared also to indicate dependency
     * (duplicate installers registrations will be removed).
     *
     * @param installers feature installer classes to register
     * @return bootstrap instance for chained calls
     */
    @SafeVarargs
    public final GuiceyBootstrap installers(final Class<? extends FeatureInstaller>... installers) {
        context.registerInstallers(installers);
        return this;
    }

    /**
     * Bundle should not rely on auto-scan mechanism and so must declare all extensions manually
     * (this better declares bundle content and speed ups startup).
     * <p>
     * NOTE: startup will fail if bean not recognized by installers.
     * <p>
     * NOTE: Don't register commands here: either enable auto scan, which will install commands automatically
     * or register command directly to bootstrap object and dependencies will be injected to them after
     * injector creation.
     *
     * @param extensionClasses extension bean classes to register
     * @return bootstrap instance for chained calls
     */
    public GuiceyBootstrap extensions(final Class<?>... extensionClasses) {
        context.registerExtensions(extensionClasses);
        return this;
    }

    /**
     * Register other guicey bundles for installation.
     * <p>
     * Duplicate bundles will be filtered automatically: bundles of the same type considered duplicate
     * (if two or more bundles of the same type detected then only first instance will be processed).
     *
     * @param bundles guicey bundles
     * @return bootstrap instance for chained calls
     */
    public GuiceyBootstrap bundles(final GuiceyBundle... bundles) {
        context.registerBundles(bundles);
        iterationBundles.addAll(Arrays.asList(bundles));
        return this;
    }

    /**
     * @param installers feature installer types to disable
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableInstallers(Class[])
     */
    @SafeVarargs
    public final GuiceyBootstrap disableInstallers(final Class<? extends FeatureInstaller>... installers) {
        context.disableInstallers(installers);
        return this;
    }

    /**
     * @param extensions extensions to disable (manually added, registered by bundles or with classpath scan)
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableExtensions(Class[])
     */
    public final GuiceyBootstrap disableExtensions(final Class<?>... extensions) {
        context.disableExtensions(extensions);
        return this;
    }

    /**
     * Disable both usual and overriding guice modules.
     *
     * @param modules guice module types to disable
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableModules(Class[])
     */
    @SafeVarargs
    public final GuiceyBootstrap disableModules(final Class<? extends Module>... modules) {
        context.disableModules(modules);
        return this;
    }

    /**
     * Guicey broadcast a lot of events in order to indicate lifecycle phases
     * ({@linkplain ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle}). Listener, registered in bundles
     * could listen events from {@link ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle#BundlesProcessed}.
     * <p>
     * WARNING: don't register listeners implementing
     * {@link ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook} - such registrations will be rejected
     * (it is too late - all hooks were already processed, but, as listener requires hooks support,
     * assuming it can't work without proper configuration).
     *
     * @param listeners guicey lifecycle listeners
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#listen(GuiceyLifecycleListener...)
     */
    public GuiceyBootstrap listen(final GuiceyLifecycleListener... listeners) {
        context.lifecycle().register(listeners);
        return this;
    }
}
