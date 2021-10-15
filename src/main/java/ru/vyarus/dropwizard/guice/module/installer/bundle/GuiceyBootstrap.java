package ru.vyarus.dropwizard.guice.module.installer.bundle;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.option.Option;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener;

import java.util.List;
import java.util.function.Supplier;

/**
 * Guicey initialization object. Provides almost the same configuration methods as
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder}. Also, contains dropwizard bootstrap objects.
 * May register pure dropwizard bundles.
 * <p>
 * In contrast to main builder, guicey bundle can't:
 * <ul>
 * <li>Disable bundles (because at this stage bundles already partly processed)</li>
 * <li>Use generic disable predicates (to not allow bundles disable, moreover it's tests-oriented feature)</li>
 * <li>Change options (because some bundles may already apply configuration based on changed option value
 * which will mean inconsistent state)</li>
 * <li>Register listener, implementing {@link ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook}
 * (because it's too late - all hooks were processed)</li>
 * <li>Register some special objects like custom injector factory or custom bundles lookup</li>
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
     * @param <T> configuration type
     * @return dropwizard bootstrap instance
     */
    @SuppressWarnings("unchecked")
    public <T extends Configuration> Bootstrap<T> bootstrap() {
        return context.getBootstrap();
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
     * Note that this registration appear under initialization phase and so neither configuration nor environment
     * objects are not available yet. If you need them for module, then you can wrap it with
     * {@link ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule} or register modules in run phase
     * (inside {@link GuiceyBundle#run(GuiceyEnvironment)}).
     *
     * @param modules one or more guice modules
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modules(com.google.inject.Module...)
     * @see ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule
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
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#installers(Class[])
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
     * NOTE: startup will fail if bean not recognized by installers. Use {@link #extensionsOptional(Class[])} to
     * register optional extension.
     * <p>
     * Alternatively, you can manually bind extensions in guice module and they would be recognized
     * ({@link ru.vyarus.dropwizard.guice.GuiceyOptions#AnalyzeGuiceModules}).
     *
     * @param extensionClasses extension bean classes to register
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#extensions(Class[])
     */
    public GuiceyBootstrap extensions(final Class<?>... extensionClasses) {
        context.registerExtensions(extensionClasses);
        return this;
    }

    /**
     * The same as {@link #extensions(Class[])}, but, in case if no installer recognize extension, will be
     * automatically disabled instead of throwing error. Useful for optional extensions declaration in 3rd party
     * bundles (where it is impossible to be sure what other bundles will be used and so what installers will
     * be available).
     * <p>
     * Alternatively, you can manually bind extensions in guice module and they would be recognized
     * ({@link ru.vyarus.dropwizard.guice.GuiceyOptions#AnalyzeGuiceModules}). Extensions with no available target
     * installer will simply wouldn't be detected (because installers used for recognition) and so there is no need
     * to mark them as optional in this case.
     *
     * @param extensionClasses extension bean classes to register
     * @return bootstrap instance for chained calls
     */
    public GuiceyBootstrap extensionsOptional(final Class<?>... extensionClasses) {
        context.registerExtensionsOptional(extensionClasses);
        return this;
    }

    /**
     * Register other guicey bundles for installation.
     * <p>
     * Equal instances of the same type will be considered as duplicate.
     *
     * @param bundles guicey bundles
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#bundles(GuiceyBundle...)
     */
    public GuiceyBootstrap bundles(final GuiceyBundle... bundles) {
        // remember only non duplicate bundles
        iterationBundles.addAll(context.registerBundles(bundles));
        return this;
    }

    /**
     * Shortcut for dropwizard bundles registration (instead of {@code bootstrap().addBundle()}), but with
     * duplicates detection and tracking in diagnostic reporting. Dropwizard bundle is immediately initialized.
     *
     * @param bundles dropwizard bundles to register
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#dropwizardBundles(ConfiguredBundle...)
     */
    public GuiceyBootstrap dropwizardBundles(final ConfiguredBundle... bundles) {
        context.registerDropwizardBundles(bundles);
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
     * <p>
     * If bindings analysis is not disabled, could also disable inner (transitive) modules, but only inside
     * normal modules.
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
     * could listen events from {@link ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle#BundlesInitialized}.
     * <p>
     * Listener is not registered if equal listener was already registered ({@link java.util.Set} used as
     * listeners storage), so if you need to be sure that only one instance of some listener will be used
     * implement {@link Object#equals(Object)}.
     *
     * @param listeners guicey lifecycle listeners
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#listen(GuiceyLifecycleListener...)
     */
    public GuiceyBootstrap listen(final GuiceyLifecycleListener... listeners) {
        context.lifecycle().register(listeners);
        return this;
    }

    /**
     * Share global state to be used in other bundles (during configuration). This was added for very special cases
     * when shared state is unavoidable to not re-invent the wheel each time!
     * <p>
     * Internally, state is linked to application instance, so it would be safe to use with concurrent tests.
     * Value could be accessed statically with application instance:
     * {@link ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState#lookup(Application, Class)}.
     * <p>
     * Use bundle class as key. Value could be set only once (to prevent hard to track situations).
     * <p>
     * If initialization point could vary (first access should initialize it) use {@link #sharedState(Class, Supplier)}
     * instead.
     *
     * @param key   shared object key
     * @param value shared object
     * @return bootstrap instance for chained calls
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    public GuiceyBootstrap shareState(final Class<?> key, final Object value) {
        context.getSharedState().put(key, value);
        return this;
    }

    /**
     * Alternative shared value initialization for cases when first accessed bundle should init state value
     * and all other just use it.
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
     * Use to access shared state value and immediately fail if value not yet set (most likely due to incorrect
     * configuration order).
     *
     * @param key     shared object key
     * @param message exception message (could use {@link String#format(String, Object...)} placeholders)
     * @param args    placeholder arguments for error message
     * @param <T>     shared object type
     * @return shared object
     * @throws IllegalStateException if no value available
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    public <T> T sharedStateOrFail(final Class<?> key, final String message, final Object... args) {
        return context.getSharedState().getOrFail(key, message, args);
    }
}
