package ru.vyarus.dropwizard.guice.module.support;

import com.google.inject.AbstractModule;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigTreeBuilder;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Base module to avoid boilerplate. It's not required to extend it, but
 * useful if dropwizard objects required in module: no need to manually implement interfaces.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueDropwizardAwareModule for uniquness
 * @since 06.06.2015
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public abstract class DropwizardAwareModule<C extends Configuration> extends AbstractModule implements
        EnvironmentAwareModule,
        BootstrapAwareModule<C>,
        ConfigurationAwareModule<C>,
        ConfigurationTreeAwareModule,
        OptionsAwareModule {

    private static final String STATE_NOT_FOUND = "Shared state not found";

    private C configuration;
    private Bootstrap<C> bootstrap;
    private Environment environment;
    private Options options;
    private ConfigurationTree configurationTree;

    @Override
    public void setConfiguration(final C configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setBootstrap(final Bootstrap<C> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setOptions(final Options options) {
        this.options = options;
    }

    @Override
    public void setConfigurationTree(final ConfigurationTree configurationTree) {
        this.configurationTree = configurationTree;
    }

    /**
     * @return application bootstrap object
     */
    protected Bootstrap<C> bootstrap() {
        return bootstrap;
    }

    /**
     * @return application configuration
     */
    protected C configuration() {
        return configuration;
    }

    /**
     * May be used to access current configuration value by exact path. This is helpful for modules universality:
     * suppose bundle X requires configuration object XConf, which is configured somewhere inside application
     * configuration. We can require configuration path in module constructor and use it to access required
     * configuration object: {@code new X("sub.config.path")}.
     *
     * @param yamlPath target value yaml path
     * @param <T>      value type
     * @return configuration value by path or null if value is null or path not exists
     * @see #configurationTree() for custom configuration searches
     */
    protected <T> T configuration(final String yamlPath) {
        return configurationTree().valueByPath(yamlPath);
    }

    /**
     * May be used to access unique sub configuration object. This is helpful for modules universality:
     * suppose module X requires configuration object XConf and we are sure that only one declaration of XConf would
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
     * {@code configuration(ServerFactory.class) == <DefaultServerFactory> (or SimpleServerFactory)}
     * (see dropwizard {@link Configuration} class).
     *
     * @param type target configuration declaration type
     * @param <T>  declaration type
     * @param <K>  required value type (may be the same or extending type)
     * @return unique configuration value or null if value is null or no declaration found
     * @see #configurationTree() for custom configuration searches
     */
    protected <T, K extends T> K configuration(final Class<T> type) {
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
     * universal module.
     * <p>
     * Note: only custom types may be used (sub configuration objects), not Integer, Boolean, List, etc.
     *
     * @param type target configuration type
     * @param <T>  value type
     * @return list of configuration values with required type or empty list
     * @see #configurationTree() for custom configuration searches
     */
    protected <T> List<? extends T> configurations(final Class<T> type) {
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
    protected ConfigurationTree configurationTree() {
        return configurationTree;
    }

    /**
     * @return application environment
     */
    protected Environment environment() {
        return environment;
    }

    /**
     * @return application class package (most likely root package for entire application)
     */
    protected String appPackage() {
        return bootstrap().getApplication().getClass().getPackage().getName();
    }

    /**
     * @return options accessor object
     */
    protected Options options() {
        return options;
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
     * {@link ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState#lookup(
     * io.dropwizard.core.Application, Class)}.
     * <p>
     * During application strartup, shared state could be requested with a static call
     * {@link ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState#getStartupInstance()}, but only
     * from main thread.
     * <p>
     * In some cases, it is preferred to use module class as key. Value could be set only once
     * (to prevent hard to track situations)
     * <p>
     * If initialization point could vary (first access should initialize it) use
     * {@link #sharedState(Class, java.util.function.Supplier)} instead.
     *
     * @param key   shared object key
     * @param value shared object
     * @param <V> shared object type
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    public <V> void shareState(final Class<V> key, final V value) {
        SharedConfigurationState.getOrFail(environment(), STATE_NOT_FOUND).put(key, value);
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
    public <T> T sharedState(final Class<T> key, final Supplier<T> defaultValue) {
        return SharedConfigurationState.getOrFail(environment(), STATE_NOT_FOUND).get(key, defaultValue);
    }

    /**
     * Access shared value. Shared state value assumed to be initialized under initialization phase by bundle (but you
     * can workaround this limitation by accessing shared state statically with
     * {@link ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState})
     *
     * @param key shared object key
     * @param <T> shared object type
     * @return shared object
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    protected <T> Optional<T> sharedState(final Class<T> key) {
        return SharedConfigurationState.lookup(environment(), key);
    }

    /**
     * Used to access shared state value and immediately fail if value not yet set (most likely, due to incorrect
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
    protected <T> T sharedStateOrFail(final Class<T> key, final String message, final Object... args) {
        return SharedConfigurationState.lookupOrFail(environment(), key, message, args);
    }

    /**
     * Reactive shared value access: if value already available action called immediately, otherwise action would
     * be called when value set (note that value could be set only once).
     *
     * @param key    shared object key
     * @param action action to execute when value would be set
     * @param <V>    value type
     */
    protected <V> void whenSharedStateReady(final Class<V> key, final Consumer<V> action) {
        SharedConfigurationState.getOrFail(environment(), "Shared state not available").whenReady(key, action);
    }
}
