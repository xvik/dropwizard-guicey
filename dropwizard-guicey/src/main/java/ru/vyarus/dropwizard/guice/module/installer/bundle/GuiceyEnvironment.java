package ru.vyarus.dropwizard.guice.module.installer.bundle;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.option.Option;
import ru.vyarus.dropwizard.guice.module.installer.bundle.listener.ApplicationShutdownListener;
import ru.vyarus.dropwizard.guice.module.installer.bundle.listener.ApplicationShutdownListenerAdapter;
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
import java.util.function.Consumer;
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
@SuppressWarnings({"PMD.TooManyMethods", "ClassFanOutComplexity", "PMD.CouplingBetweenObjects"})
public class GuiceyEnvironment implements GuiceyCommonRegistration<GuiceyEnvironment> {

    private final ConfigurationContext context;

    /**
     * Create environment.
     *
     * @param context configuration context
     */
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
     * @param <C>      configuration type
     * @return builder instance for chained calls
     */
    public <C extends Configuration> GuiceyEnvironment onGuiceyStartup(final GuiceyStartupListener<C> listener) {
        return listen(new GuiceyStartupListenerAdapter<>(listener));
    }

    /**
     * Code to execute after complete application startup. For server command it would happen after jetty startup
     * and for lightweight guicey test helpers ({@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp}) - after
     * guicey start (as jetty not started in this case). In both cases, application completely started at this moment.
     * Suitable for reporting.
     * <p>
     * If you need to listen only for real server startup then use
     * {@link #listenServer(io.dropwizard.lifecycle.ServerLifecycleListener)} instead.
     * <p>
     * Not called on custom command execution (because no lifecycle involved in this case). In this case you can use
     * {@link #onGuiceyStartup(GuiceyStartupListener)} as always executed point.
     *
     * @param listener listener to call on server startup
     * @return builder instance for chained calls
     */
    public GuiceyEnvironment onApplicationStartup(final ApplicationStartupListener listener) {
        return listen(new ApplicationStartupListenerAdapter(listener));
    }

    /**
     * Code to execute after complete application shutdown. Called not only for real application but for
     * environment commands and lightweight guicey test helpers
     * ({@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp}). Suitable for closing additional resources.
     * <p>
     * If you need to listen only for real server shutdown then use
     * {@link #listenServer(io.dropwizard.lifecycle.ServerLifecycleListener)} instead.
     * <p>
     * Not called on command execution because no lifecycle involved in this case.
     *
     * @param listener listener to call on server startup
     * @return builder instance for chained calls
     */
    public GuiceyEnvironment onApplicationShutdown(final ApplicationShutdownListener listener) {
        return listen(new ApplicationShutdownListenerAdapter(listener));
    }

    /**
     * Shortcut for {@code environment().lifecycle().addServerLifecycleListener} registration.
     * <p>
     * Note that server listener is called only when jetty starts up and so will not be called with lightweight
     * guicey test helpers {@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp}. Prefer using
     * {@link #onApplicationStartup(ApplicationStartupListener)} to be correctly called in tests (of course, if not
     * server-only execution is desired).
     * <p>
     * Not called for custom command execution.
     *
     * @param listener server startup listener.
     * @return builder instance for chained calls
     */
    public GuiceyEnvironment listenServer(final ServerLifecycleListener listener) {
        environment().lifecycle().addServerLifecycleListener(listener);
        return this;
    }

    /**
     * Shortcut for jetty lifecycle listener {@code environment().lifecycle().addEventListener(listener)}
     * registration.
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
     * @return builder instance for chained calls
     */
    public GuiceyEnvironment listenJetty(final LifeCycle.Listener listener) {
        environment().lifecycle().addEventListener(listener);
        return this;
    }

    /**
     * Shortcut for jetty events and requests listener {@code environment().jersey().register(listener)}
     * registration.
     * <p>
     * Listeners are not called on custom command execution.
     *
     * @param listener listener instance
     * @return builder instance for chained calls
     */
    public GuiceyEnvironment listenJersey(final ApplicationEventListener listener) {
        environment().jersey().register(listener);
        return this;
    }

    // ------------------------------------------------------------------ COMMON METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K extends Configuration> Bootstrap<K> bootstrap() {
        return context.getBootstrap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K extends Configuration> Application<K> application() {
        return context.getBootstrap().getApplication();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V, K extends Enum<? extends Option> & Option> V option(final K option) {
        return context.option(option);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuiceyEnvironment modules(final Module... modules) {
        Preconditions.checkState(modules.length > 0, "Specify at least one module");
        context.registerModules(modules);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuiceyEnvironment modulesOverride(final Module... modules) {
        context.registerModulesOverride(modules);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuiceyEnvironment extensions(final Class<?>... extensionClasses) {
        context.registerExtensions(extensionClasses);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuiceyEnvironment extensionsOptional(final Class<?>... extensionClasses) {
        context.registerExtensionsOptional(extensionClasses);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuiceyEnvironment disableExtensions(final Class<?>... extensions) {
        context.disableExtensions(extensions);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SafeVarargs
    public final GuiceyEnvironment disableModules(final Class<? extends Module>... modules) {
        context.disableModules(modules);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuiceyEnvironment listen(final GuiceyLifecycleListener... listeners) {
        context.lifecycle().register(listeners);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K> GuiceyEnvironment shareState(final Class<K> key, final K value) {
        context.getSharedState().put(key, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K> K sharedState(final Class<K> key, final Supplier<K> defaultValue) {
        return context.getSharedState().get(key, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K> Optional<K> sharedState(final Class<K> key) {
        return Optional.ofNullable(context.getSharedState().get(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K> K sharedStateOrFail(final Class<K> key, final String message, final Object... args) {
        return context.getSharedState().getOrFail(key, message, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V> void whenSharedStateReady(final Class<V> key, final Consumer<V> action) {
        context.getSharedState().whenReady(key, action);
    }
}
