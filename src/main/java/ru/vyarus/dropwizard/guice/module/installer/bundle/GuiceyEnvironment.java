package ru.vyarus.dropwizard.guice.module.installer.bundle;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.option.Option;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigTreeBuilder;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

import java.util.List;

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
    public GuiceyEnvironment register(Object... items) {
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
    public GuiceyEnvironment register(Class<?>... items) {
        for (Class<?> item : items) {
            environment().jersey().register(item);
        }
        return this;
    }
}
