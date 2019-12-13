package ru.vyarus.dropwizard.guice.module.support;

import com.google.inject.AbstractModule;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigTreeBuilder;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

import java.util.List;
import java.util.Optional;

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
     * Use to access shared state value and immediately fail if value not yet set (most likely due to incorrect
     * configuration order).
     * <p>
     * Note: shared state value assumed to be initialized under initialization phase by bundle (but you can workaround
     * this limitation by accessing shared state statically with
     * {@link ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState})
     *
     * @param key     shared object key
     * @param message exception message (could use {@link String#format(String, Object...)} placeholders)
     * @param args    placeholder arguments for error message
     * @param <T>     shared object type
     * @return shared object
     * @throws IllegalStateException if not value available
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    protected <T> T sharedStateOrFail(final Class<?> key, final String message, final Object... args) {
        return SharedConfigurationState.lookupOrFail(environment(), key, message, args);
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
    protected <T> Optional<T> sharedState(final Class<?> key) {
        return SharedConfigurationState.lookup(environment(), key);
    }
}
