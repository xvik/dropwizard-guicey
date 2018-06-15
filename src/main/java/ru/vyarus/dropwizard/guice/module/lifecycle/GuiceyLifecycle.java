package ru.vyarus.dropwizard.guice.module.lifecycle;

import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfiguratorsProcessedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.InitializationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HK2ConfigurationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HK2ExtensionsInstalledByEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HK2ExtensionsInstalledEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.*;

/**
 * Guicey lifecycle events. Enum should be used for differentiation of events in {@link GuiceyLifecycleListener}:
 * {@link GuiceyLifecycleEvent#getType()}.
 * <p>
 * Events specified in execution order.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public enum GuiceyLifecycle {

    // -- Bundle.initialize()

    /**
     * Called after all registered {@link ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator}
     * processing. Provides all instances of executed configurators. Not called if no configurators used.
     */
    ConfiguratorsProcessed(ConfiguratorsProcessedEvent.class),
    /**
     * Called after {@link ru.vyarus.dropwizard.guice.GuiceBundle#initialize(io.dropwizard.setup.Bootstrap)} method end.
     * Just a convenient moment to apply registrations into dropwizard {@link io.dropwizard.setup.Bootstrap} object
     * from listener.
     * <p>
     * If commands search is enabled, then all commands found in classpath will be provided in event.
     * <p>
     * Consider this point as somewhere inside of your application's
     * {@link io.dropwizard.Application#initialize(io.dropwizard.setup.Bootstrap)}.
     */
    Initialization(InitializationEvent.class),

    // -- Bundle.run()

    /**
     * Special meta event, called before all guice bundle run phase logic (when configuration and environment are
     * already available). Could be used to print some diagnostic info before guicey initialization
     * (for example, available configuration bindings to debug guice injector creation failure due to missed bindings).
     */
    BeforeRun(BeforeRunEvent.class),

    /**
     * Called if configuration from dw bundles enabled and at least one bundle recognized. Provides list of
     * recognized bundles (note: some of these bundles could be actually disabled and not used further).
     */
    BundlesFromDwResolved(BundlesFromDwResolvedEvent.class),
    /**
     * Called if at least one bundle recognized using bundles lookup. Provides list of recognized bundles
     * (note: some of these bundles could be disabled and not used further).
     */
    BundlesFromLookupResolved(BundlesFromLookupResolvedEvent.class),
    /**
     * Called after {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup} and resolution form dropwizard
     * bundles mechanisms when all top-level bundles are resolved. Provides a list of all enabled and list of disabled
     * bundles. Called even if no bundles registered to indicate configuration state.
     */
    BundlesResolved(BundlesResolvedEvent.class),
    /**
     * Called after bundles processing. Note that bundles could register other bundles and so resulted
     * list of installed bundles could be bigger (than in resolution event). Provides a list of all used and all
     * disabled bundles. Not called even if no bundles were used at all (no processing - no event).
     */
    BundlesProcessed(BundlesProcessedEvent.class),
    /**
     * Called just before guice injector creation. Provides all configured modules (main and override) and all
     * disabled modules. Called even when no modules registered to indicate configuration state.
     */
    InjectorCreation(InjectorCreationEvent.class),
    /**
     * Called when installers resolved (from classpath scan, if enabled) and initialized. Provides list of all
     * enabled and list of all disabled installers (which will be used for extensions recognition and installation).
     * Called even if no installers are resolved to indicate configuration state.
     * <p>
     * Guice context is creating at that moment.
     */
    InstallersResolved(InstallersResolvedEvent.class),
    /**
     * Called when all extensions detected (from classpath scan, if enabled). Provides list of all enabled
     * and list of disabled extension types (instances are not available yet). Called even if no extensions
     * configured to indicate configuration state.
     * <p>
     * Guice context is creating at that moment.
     */
    ExtensionsResolved(ExtensionsResolvedEvent.class),
    /**
     * Called when installer installed all related extensions and only for installers actually performed
     * installations (extensions list never empty). Provides installer and installed extensions types.
     * <p>
     * NOTE: {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} installers will not be
     * notified here, even if they participate in installation it is considered as incomplete at that point.
     * <p>
     * Extension instance could be obtained manually from injector. Injector is available because it's already
     * constructed, but singletons initialization is still in progress.
     */
    ExtensionsInstalledBy(ExtensionsInstalledByEvent.class),
    /**
     * Called after all installers install related extensions.
     * Provides list of all used (enabled) extensions. Not called when no extensions installed.
     * <p>
     * Extension instance could be obtained manually from injector. Injector is available because it's already
     * constructed, but singletons initialization is still in progress.
     */
    ExtensionsInstalled(ExtensionsInstalledEvent.class),
    /**
     * Called after
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle#run(io.dropwizard.Configuration, io.dropwizard.setup.Environment)}
     * when guicey context is started, extensions installed (but not hk extensions, because neither jersey nor jetty
     * is't start yet).
     * <p>
     * At this point injection to registered commands is performed (this may be important if custom command
     * run application instead of "server"). Injector itself is completely initialized - all singletons started.
     * <p>
     * This point is before
     * {@link io.dropwizard.Application#run(io.dropwizard.Configuration, io.dropwizard.setup.Environment)}. Ideal point
     * for jersey and jetty listeners installation (with shortcut event methods).
     */
    ApplicationRun(ApplicationRunEvent.class),

    // -- Application.run()

    /**
     * HK2 context starting. At this point jersey is starting and jetty is only initializing. Since that point
     * HK2 {@link org.glassfish.hk2.api.ServiceLocator} is accessible.
     */
    HK2Configuration(HK2ConfigurationEvent.class),
    /**
     * Called when {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} installer installed all
     * related extensions and only for installers actually performed installations (extensions list never empty).
     * Provides installer and installed extensions types.
     * <p>
     * At this point HK2 is not completely started and so HK2 managed extensions
     * ({@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed}) couldn't be obtained yet
     * (even though you have access to root service locator). But extensions managed by guice could be obtained
     * from guice context.
     */
    HK2ExtensionsInstalledBy(HK2ExtensionsInstalledByEvent.class),
    /**
     * Called after all {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} installers install
     * related extensions and only when at least one extension was installed. Provides list of all used (enabled)
     * extensions.
     * <p>
     * At this point HK2 is not completely started and so HK2 managed extensions
     * ({@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed}) couldn't be obtained yet
     * (even though you have access to root service locator). But extensions managed by guice could be obtained
     * from guice context.
     * <p>
     * To listen HK2 lifecycle further use jersey events (like in
     * {@link ru.vyarus.dropwizard.guice.module.lifecycle.debug.DebugGuiceyLifecycle}).
     */
    HK2ExtensionsInstalled(HK2ExtensionsInstalledEvent.class);

    private final Class<? extends GuiceyLifecycleEvent> type;

    GuiceyLifecycle(final Class<? extends GuiceyLifecycleEvent> type) {
        this.type = type;
    }

    /**
     * @return type of related event
     */
    public Class<? extends GuiceyLifecycleEvent> getType() {
        return type;
    }
}
