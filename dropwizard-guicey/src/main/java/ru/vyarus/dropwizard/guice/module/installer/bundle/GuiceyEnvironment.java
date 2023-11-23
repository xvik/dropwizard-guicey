package ru.vyarus.dropwizard.guice.module.installer.bundle;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.LifeCycle;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.option.Option;
import ru.vyarus.dropwizard.guice.module.installer.bundle.listener.ApplicationStartupListener;
import ru.vyarus.dropwizard.guice.module.installer.bundle.listener.ApplicationStartupListenerAdapter;
import ru.vyarus.dropwizard.guice.module.installer.bundle.listener.GuiceyStartupListener;
import ru.vyarus.dropwizard.guice.module.installer.bundle.listener.GuiceyStartupListenerAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigTreeBuilder;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Guicey environment object. Provides almost the same configuration methods as
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder}. Also, contains dropwizard configuration and
 * environment objects.
 * <p>
 * As it is called on run phase, it does not allow to install or disable new bundles and installers.
 *
 * @author Vyacheslav Rusakov
 * @since 13.06.2019
 */
@SuppressWarnings("PMD.TooManyMethods")
public class GuiceyEnvironment {

    private final ConfigurationContext context;

    public GuiceyEnvironment(final ConfigurationContext context) {
        this.context = context;
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
     * @param <T>      value type
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
     * @param <T>  declaration type
     * @param <K>  required value type (may be the same or extending type)
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
     * @param <T>  value type
     * @return list of configuration values with required type or empty list
     * @see #configurationTree() for custom configuration searches
     */
    public <T> List<? extends T> configurations(final Class<T> type) {
        return configurationTree().valuesByType(type);
    }

    /**
     * Search for exactly one annotated configuration value. It is not possible to provide the exact annotation
     * instance, but you can create a class implementing annotation and use it for search. For example, guice
     * {@link com.google.inject.name.Named} annotation has {@link com.google.inject.name.Names#named(String)}:
     * it is important that real annotation instance and "pseudo" annotation object would be equal.
     * <p>
     * For annotations without attributes use annotation type: {@link #annotatedConfiguration(Class)}.
     * <p>
     * For multiple values use {@code configurationTree().annotatedValues()}.
     *
     * @param annotation annotation instance (equal object) to search for an annotated config path
     * @param <T>        value type
     * @return qualified configuration value or null
     * @throws java.lang.IllegalStateException if multiple values found
     */
    protected <T> T annotatedConfiguration(final Annotation annotation) {
        return configurationTree().annotatedValue(annotation);
    }

    /**
     * Search for exactly one configuration value with qualifier annotation (without attributes). For cases when
     * annotation with attributes used - use {@link #annotatedConfiguration(java.lang.annotation.Annotation)}
     * (current method would search only by annotation type, ignoring any (possible) attributes).
     * <p>
     * For multiple values use {@code configurationTree().annotatedValues()}.
     *
     * @param qualifierType qualifier annotation type
     * @param <T>           value type
     * @return qualified configuration value or null
     * @throws java.lang.IllegalStateException if multiple values found
     */
    protected <T> T annotatedConfiguration(final Class<? extends Annotation> qualifierType) {
        return configurationTree().annotatedValue(qualifierType);
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
     * Register guice modules.
     * <p>
     * Note that this registration appear in run phase and so you already have access
     * to environment and configuration (and don't need to use Aware* interfaces, but if you will they will also
     * work, of course). This may look like misconception because configuration appear not in configuration phase,
     * but it's not: for example, in pure dropwizard you can register jersey configuration modules in run phase too.
     * This brings the simplicity of use: 3rd party guice modules often require configuration values to
     * be passed directly to constructor, which is impossible in initialization phase (and so you have to use Aware*
     * workarounds).
     *
     * @param modules one or more guice modules
     * @return environment instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modules(com.google.inject.Module...)
     */
    public GuiceyEnvironment modules(final Module... modules) {
        Preconditions.checkState(modules.length > 0, "Specify at least one module");
        context.registerModules(modules);
        return this;
    }

    /**
     * Override modules (using guice {@link com.google.inject.util.Modules#override(Module...)}).
     *
     * @param modules overriding modules
     * @return environment instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modulesOverride(Module...)
     */
    public GuiceyEnvironment modulesOverride(final Module... modules) {
        context.registerModulesOverride(modules);
        return this;
    }

    /**
     * @param extensions extensions to disable (manually added, registered by bundles or with classpath scan)
     * @return environment instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableExtensions(Class[])
     */
    public final GuiceyEnvironment disableExtensions(final Class<?>... extensions) {
        context.disableExtensions(extensions);
        return this;
    }

    /**
     * Disable both usual and overriding guice modules.
     * <p>
     * If bindings analysis is not disabled, could also disable inner (transitive) modules, but only inside
     * normal modules.
     *
     * @param modules guice module types to disable
     * @return environment instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableModules(Class[])
     */
    @SafeVarargs
    public final GuiceyEnvironment disableModules(final Class<? extends Module>... modules) {
        context.disableModules(modules);
        return this;
    }

    /**
     * Shortcut for {@code environment().jersey().register()} for direct registration of jersey extensions.
     * For the most cases prefer automatic installation of jersey extensions with guicey installer.
     *
     * @param items jersey extension instances to install
     * @return environment instance for chained calls
     */
    public GuiceyEnvironment register(final Object... items) {
        for (Object item : items) {
            environment().jersey().register(item);
        }
        return this;
    }

    /**
     * Shortcut for {@code environment().jersey().register()} for direct registration of jersey extensions.
     * For the most cases prefer automatic installation of jersey extensions with guicey installer.
     *
     * @param items jersey extension instances to install
     * @return environment instance for chained calls
     */
    public GuiceyEnvironment register(final Class<?>... items) {
        for (Class<?> item : items) {
            environment().jersey().register(item);
        }
        return this;
    }

    /**
     * Shortcut for manual registration of {@link Managed} objects.
     * <p>
     * Pay attention that managed objects are not called for commands.
     *
     * @param managed managed to register
     * @return environment instance for chained calls
     */
    public GuiceyEnvironment manage(final Managed managed) {
        environment().lifecycle().manage(managed);
        return this;
    }

    /**
     * Guicey broadcast a lot of events in order to indicate lifecycle phases
     * ({@linkplain ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle}). Listener, registered in run phase
     * could listen events from {@link ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle#BundlesStarted}.
     * <p>
     * Listener is not registered if equal listener was already registered ({@link java.util.Set} used as
     * listeners storage), so if you need to be sure that only one instance of some listener will be used
     * implement {@link Object#equals(Object)}.
     *
     * @param listeners guicey lifecycle listeners
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#listen(GuiceyLifecycleListener...)
     */
    public GuiceyEnvironment listen(final GuiceyLifecycleListener... listeners) {
        context.lifecycle().register(listeners);
        return this;
    }

    /**
     * Shortcut for {@link ServerLifecycleListener} registration.
     * <p>
     * Note that server listener is called only when jetty starts up and so will not be called with lightweight
     * guicey test helpers {@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp}. Prefer using
     * {@link #onApplicationStartup(ApplicationStartupListener)} to be correctly called in tests (of course, if not
     * server only execution is desired).
     * <p>
     * Obviously not called for custom command execution.
     *
     * @param listener server startup listener.
     * @return environment instance for chained calls
     */
    public GuiceyEnvironment listenServer(final ServerLifecycleListener listener) {
        environment().lifecycle().addServerLifecycleListener(listener);
        return this;
    }

    /**
     * Shortcut for jetty lifecycle listener {@link LifeCycle.Listener listener} registration.
     * <p>
     * Lifecycle listeners are called with lightweight guicey test helpers
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp} which makes them perfectly suitable for reporting.
     * <p>
     * If only startup event is required, prefer {@link #onApplicationStartup(ApplicationStartupListener)} method
     * as more expressive and easier to use.
     * <p>
     * Listeners are not called on custom command execution.
     *
     * @param listener jetty
     * @return environment instance for chained calls
     */
    public GuiceyEnvironment listenJetty(final LifeCycle.Listener listener) {
        environment().lifecycle().addLifeCycleListener(listener);
        return this;
    }

    /**
     * Share global state to be used in other bundles (during configuration). This was added for very special cases
     * when shared state is unavoidable (to not re-invent the wheel each time)!
     * <p>
     * It is preferred to initialize shared state under initialization phase to avoid problems related to
     * initialization order (assuming state is used under run phase). But, in some cases, it is not possible.
     * <p>
     * Internally, state is linked to application instance, so it would be safe to use with concurrent tests.
     * Value could be accessed statically with application instance:
     * {@link ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState#lookup(Application, Class)}.
     * <p>
     * During application strartup, shared state could be requested with a static call
     * {@link ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState#getStartupInstance()}, but only
     * from main thread.
     * <p>
     * In some cases, it is preferred to use bundle class as key. Value could be set only once
     * (to prevent hard to track situations).
     * <p>
     * If initialization point could vary (first access should initialize it) use
     * {@link #sharedState(Class, java.util.function.Supplier)} instead.
     *
     * @param key   shared object key
     * @param value shared object
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    public GuiceyEnvironment shareState(final Class<?> key, final Object value) {
        context.getSharedState().put(key, value);
        return this;
    }

    /**
     * Alternative shared value initialization for cases when first accessed bundle should init state value
     * and all other just use it.
     * <p>
     * It is preferred to initialize shared state under initialization phase to avoid problems related to
     * initialization order (assuming state is used under run phase). But, in some cases, it is not possible.
     *
     * @param key          shared object key
     * @param defaultValue default object provider
     * @param <T>          shared object type
     * @return shared object (possibly just created)
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    public <T> T sharedState(final Class<?> key, final Supplier<T> defaultValue) {
        return context.getSharedState().get(key, defaultValue);
    }

    /**
     * Access shared value.
     *
     * @param key shared object key
     * @param <T> shared object type
     * @return shared object
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    public <T> Optional<T> sharedState(final Class<?> key) {
        return Optional.ofNullable(context.getSharedState().get(key));
    }

    /**
     * Used to access shared state value and immediately fail if value not yet set (most likely due to incorrect
     * configuration order).
     *
     * @param key     shared object key
     * @param message exception message (could use {@link String#format(String, Object...)} placeholders)
     * @param args    placeholder arguments for error message
     * @param <T>     shared object type
     * @return shared object
     * @throws IllegalStateException if not value available
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    public <T> T sharedStateOrFail(final Class<?> key, final String message, final Object... args) {
        return context.getSharedState().getOrFail(key, message, args);
    }

    /**
     * Code to execute after guice injector creation (but still under run phase). May be used for manual
     * configurations (registrations into dropwizard environment).
     * <p>
     * Listener will be called on environment command start too.
     * <p>
     * Note: there is no registration method for this listener in main guice bundle builder
     * ({@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder}) because it is assumed, that such blocks would
     * always be wrapped with bundles to improve application readability.
     *
     * @param listener listener to call after injector creation
     * @return environment instance for chained calls
     */
    public GuiceyEnvironment onGuiceyStartup(final GuiceyStartupListener listener) {
        return listen(new GuiceyStartupListenerAdapter(listener));
    }

    /**
     * Code to execute after complete application startup. For server command it would happen after jetty startup
     * and for lightweight guicey test helpers ({@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp}) - after
     * guicey start (as jetty not started in this case). In both cases, application completely started at this moment.
     * Suitable for reporting.
     * <p>
     * If you need to listen only for real server startup then use {@link #listenServer(ServerLifecycleListener)}
     * instead.
     * <p>
     * Not called on custom command execution (because no lifecycle involved in this case). In this case you can use
     * {@link #onGuiceyStartup(GuiceyStartupListener)} as always executed point.
     *
     * @param listener listener to call on server startup
     * @return environment instance for chained calls
     */
    public GuiceyEnvironment onApplicationStartup(final ApplicationStartupListener listener) {
        return listen(new ApplicationStartupListenerAdapter(listener));
    }

}
