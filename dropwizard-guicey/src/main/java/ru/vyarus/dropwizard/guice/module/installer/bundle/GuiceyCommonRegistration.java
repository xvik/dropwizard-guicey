package ru.vyarus.dropwizard.guice.module.installer.bundle;

import com.google.inject.Module;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import ru.vyarus.dropwizard.guice.module.context.option.Option;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Common methods for {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap} and
 * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment} objects (common for configuration and
 * run phases).
 * <p>
 * Not implemented as base class to safe backwards compatibility (otherwise all existing bundles would have to be
 * re-compiled).
 *
 * @param <T> builder type
 * @author Vyacheslav Rusakov
 * @since 14.03.2025
 */
public interface GuiceyCommonRegistration<T> {

    /**
     * Note: for application in run phase (when called from
     * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment}), it would be too late to
     * configure dropwizard bootstrap object.
     *
     * @param <K> configuration type
     * @return dropwizard bootstrap instance
     */
    <K extends Configuration> Bootstrap<K> bootstrap();

    /**
     * Application instance may be useful for complex (half manual) integrations where access for
     * injector is required.
     * For example, manually registered
     * {@link io.dropwizard.lifecycle.Managed} may access injector in it's start method by calling
     * {@link ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup#getInjector(io.dropwizard.core.Application)}.
     * <p>
     * NOTE: it will work in this example, because injector access will be after injector creation.
     * Directly inside bundle initialization method injector could not be obtained as it's not exists yet.
     *
     * @return dropwizard application instance
     */
    <K extends Configuration> Application<K> application();

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
     * @param <K>    helper type to define option
     * @return assigned option value or default value
     * @see ru.vyarus.dropwizard.guice.module.context.option.Option more options info
     * @see ru.vyarus.dropwizard.guice.GuiceyOptions options example
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#option(java.lang.Enum, java.lang.Object)
     * options definition
     */
    <V, K extends Enum & Option> V option(K option);

    /**
     * Register guice modules.
     * <p>
     * When registration called under initialization phase
     * ({@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap}), neither configuration nor
     * environment objects are available yet. If you need them for module, then you can wrap it with
     * {@link ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule} or register modules in run phase
     * (inside {@link GuiceyBundle#run(GuiceyEnvironment)}).
     * <p>
     * When registration called under run phase
     * ({@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment}), environment and configuration
     * objects already available (no need to use Aware* interfaces, but if you will they will also
     * work, of course). This may look like misconception because configuration appear not in configuration phase,
     * but it's not: for example, in pure dropwizard you can register jersey configuration modules in run phase too.
     * This brings the simplicity of use: 3rd party guice modules often require configuration values to
     * be passed directly to constructor, which is impossible in initialization phase (and so you have to use Aware*
     * workarounds).
     *
     * @param modules one or more guice modules
     * @return builder instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modules(com.google.inject.Module...)
     * @see ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule
     */
    T modules(Module... modules);

    /**
     * Override modules (using guice {@link com.google.inject.util.Modules#override(com.google.inject.Module...)}).
     *
     * @param modules overriding modules
     * @return builder instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modulesOverride(com.google.inject.Module...)
     */
    T modulesOverride(Module... modules);

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
     * @return builder instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#extensions(Class[])
     */
    T extensions(Class<?>... extensionClasses);

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
     * @return builder instance for chained calls
     */
    T extensionsOptional(Class<?>... extensionClasses);

    /**
     * @param extensions extensions to disable (manually added, registered by bundles or with classpath scan)
     * @return builder instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableExtensions(Class[])
     */
    T disableExtensions(Class<?>... extensions);

    /**
     * Disable both usual and overriding guice modules.
     * <p>
     * If bindings analysis is not disabled, could also disable inner (transitive) modules, but only inside
     * normal modules.
     *
     * @param modules guice module types to disable
     * @return builder instance for chained calls
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#disableModules(Class[])
     */
    @SuppressWarnings("unchecked")
    T disableModules(Class<? extends Module>... modules);

    /**
     * Guicey broadcast a lot of events in order to indicate lifecycle phases
     * ({@link ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle}). This could be useful
     * for diagnostic logging (like {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#printLifecyclePhases()}) or
     * to implement special behaviours on installers, bundles, modules extensions (listeners have access to everything).
     * For example, {@link ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule} like support for guice
     * modules could be implemented with listeners.
     * <p>
     * Configuration items (modules, extensions, bundles) are not aware of each other and listeners
     * could be used to tie them. For example, to tell bundle if some other bundles registered (limited
     * applicability, but just for example).
     * <p>
     * You can also use {@link ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter} when you need to
     * handle multiple events (it replaces direct events handling with simple methods).
     * <p>
     * Listener is not registered if equal listener were already registered ({@link java.util.Set} used as
     * listeners storage), so if you need to be sure that only one instance of some listener will be used
     * implement {@link Object#equals(Object)} and {@link Object#hashCode()}.
     *
     * @param listeners guicey lifecycle listeners
     * @return builder instance for chained calls
     * @see ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle
     * @see ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter
     * @see ru.vyarus.dropwizard.guice.module.lifecycle.UniqueGuiceyLifecycleListener
     */
    T listen(GuiceyLifecycleListener... listeners);

    /**
     * Share global state to be used in other bundles (during configuration). This was added for very special cases
     * when shared state is unavoidable (to not re-invent the wheel each time)!
     * <p>
     * During application strartup, shared state could be requested with a static call
     * {@link ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState#getStartupInstance()}, but only
     * from main thread.
     * <p>
     * Internally, state is linked to application instance, so it would be safe to use with concurrent tests.
     * Value could be accessed statically with application instance:
     * {@link ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState#lookup(Application, Class)}.
     * <p>
     * In some cases, it is preferred to use bundle class as key. Value could be set only once
     * (to prevent hard to track situations).
     * <p>
     * If initialization point could vary (first access should initialize it) use
     * {@link #sharedState(Class, java.util.function.Supplier)} instead.
     *
     * @param key   shared object key
     * @param value shared object
     * @return builder instance for chained calls
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    T shareState(Class<?> key, Object value);

    /**
     * Alternative shared value initialization for cases when first accessed bundle should init state value
     * and all other just use it.
     * <p>
     * It is preferred to initialize shared state under initialization phase to avoid problems related to
     * initialization order (assuming state is used under run phase). But, in some cases, it is not possible.
     *
     * @param key          shared object key
     * @param defaultValue default object provider
     * @param <K>          shared object type
     * @return shared object (possibly just created)
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    <K> K sharedState(Class<?> key, Supplier<K> defaultValue);

    /**
     * Access shared value.
     *
     * @param key shared object key
     * @param <K> shared object type
     * @return shared object
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    <K> Optional<K> sharedState(Class<?> key);

    /**
     * Used to access shared state value and immediately fail if value not yet set (most likely due to incorrect
     * configuration order).
     *
     * @param key     shared object key
     * @param message exception message (could use {@link String#format(String, Object...)} placeholders)
     * @param args    placeholder arguments for error message
     * @param <K>     shared object type
     * @return shared object
     * @throws IllegalStateException if no value available
     * @see ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
     */
    <K> K sharedStateOrFail(Class<?> key, String message, Object... args);
}
