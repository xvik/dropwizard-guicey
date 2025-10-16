package ru.vyarus.dropwizard.guice.module.lifecycle;

import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.BeforeInitEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.BundlesFromLookupResolvedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.BundlesInitializedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.BundlesResolvedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ClasspathExtensionsResolvedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.CommandsResolvedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfigurationHooksProcessedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.DropwizardBundlesInitializedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.InitializedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.InstallersResolvedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ManualExtensionsValidatedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationShutdownEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartingEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStoppedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.JerseyConfigurationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.JerseyExtensionsInstalledByEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.JerseyExtensionsInstalledEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ApplicationRunEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.BeforeRunEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.BundlesStartedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ExtensionsInstalledByEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ExtensionsInstalledEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ExtensionsResolvedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.InjectorCreationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ModulesAnalyzedEvent;

/**
 * Adapter for {@link GuiceyLifecycleListener}. Useful when you need to listen for multiple events: extend adapter
 * and override just required methods.
 *
 * @author Vyacheslav Rusakov
 * @since 18.04.2018
 */
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "PMD.TooManyMethods", "PMD.CouplingBetweenObjects"})
public class GuiceyLifecycleAdapter implements GuiceyLifecycleListener {

    @Override
    @SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:MissingSwitchDefault",
            "checkstyle:JavaNCSS",
            "PMD.NcssCount", "PMD.CyclomaticComplexity"})
    public void onEvent(final GuiceyLifecycleEvent event) {
        switch (event.getType()) {
            case ConfigurationHooksProcessed:
                configurationHooksProcessed((ConfigurationHooksProcessedEvent) event);
                break;
            case BeforeInit:
                beforeInit((BeforeInitEvent) event);
                break;
            case DropwizardBundlesInitialized:
                dropwizardBundlesInitialized((DropwizardBundlesInitializedEvent) event);
                break;
            case BundlesFromLookupResolved:
                lookupBundlesResolved((BundlesFromLookupResolvedEvent) event);
                break;
            case BundlesResolved:
                bundlesResolved((BundlesResolvedEvent) event);
                break;
            case BundlesInitialized:
                bundlesInitialized((BundlesInitializedEvent) event);
                break;
            case CommandsResolved:
                commandsResolved((CommandsResolvedEvent) event);
                break;
            case InstallersResolved:
                installersResolved((InstallersResolvedEvent) event);
                break;
            case ClasspathExtensionsResolved:
                classpathExtensionsResolved((ClasspathExtensionsResolvedEvent) event);
                break;
            case Initialized:
                initialized((InitializedEvent) event);
                break;
            case BeforeRun:
                beforeRun((BeforeRunEvent) event);
                break;
            case BundlesStarted:
                bundlesStarted((BundlesStartedEvent) event);
                break;
            case ManualExtensionsValidated:
                manualExtensionsValidated((ManualExtensionsValidatedEvent) event);
                break;
            case ModulesAnalyzed:
                modulesAnalyzed((ModulesAnalyzedEvent) event);
                break;
            case ExtensionsResolved:
                extensionsResolved((ExtensionsResolvedEvent) event);
                break;
            case InjectorCreation:
                injectorCreation((InjectorCreationEvent) event);
                break;
            case ExtensionsInstalledBy:
                extensionsInstalledBy((ExtensionsInstalledByEvent) event);
                break;
            case ExtensionsInstalled:
                extensionsInstalled((ExtensionsInstalledEvent) event);
                break;
            case ApplicationRun:
                applicationRun((ApplicationRunEvent) event);
                break;
            case ApplicationStarting:
                applicationStarting((ApplicationStartingEvent) event);
                break;
            case JerseyConfiguration:
                jerseyConfiguration((JerseyConfigurationEvent) event);
                break;
            case JerseyExtensionsInstalledBy:
                jerseyExtensionsInstalledBy((JerseyExtensionsInstalledByEvent) event);
                break;
            case JerseyExtensionsInstalled:
                jerseyExtensionsInstalled((JerseyExtensionsInstalledEvent) event);
                break;
            case ApplicationStarted:
                applicationStarted((ApplicationStartedEvent) event);
                break;
            case ApplicationShutdown:
                applicationShutdown((ApplicationShutdownEvent) event);
                break;
            case ApplicationStopped:
                applicationStopped((ApplicationStoppedEvent) event);
                break;
        }
    }

    /**
     * Appeared just in time of {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#build()} after manual
     * builder configuration and all {@link ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook} processing.
     * Not called when no hooks were used.
     *
     * @param event event object
     * @see GuiceyLifecycle#ConfigurationHooksProcessed
     */
    protected void configurationHooksProcessed(final ConfigurationHooksProcessedEvent event) {
        // empty
    }

    /**
     * Special meta event, called before all {@link ru.vyarus.dropwizard.guice.GuiceBundle} configuration phase logic.
     * {@link io.dropwizard.core.setup.Bootstrap} object is available, but dropwizard bundles (registered through
     * guicey) are not yet registered (note that {@link ru.vyarus.dropwizard.guice.GuiceBundle} is not yet added to
     * bootstrap also because dropwizard calls bundle initialization before registering bundle (and so all dropwizard
     * bundles, registered by guicey, will run before {@link ru.vyarus.dropwizard.guice.GuiceBundle} run).
     *
     * @param event event object
     * @see GuiceyLifecycle#BeforeInit
     */
    protected void beforeInit(final BeforeInitEvent event) {
        // empty
    }

    /**
     * Called after dropwizard bundles initialization (for dropwizard bundles registered through guicey api).
     * Not called if no bundles were registered.
     *
     * @param event event object
     * @see GuiceyLifecycle#DropwizardBundlesInitialized
     */
    protected void dropwizardBundlesInitialized(final DropwizardBundlesInitializedEvent event) {
        // empty
    }

    /**
     * Called if at least one bundle recognized using bundles lookup. Not called at if
     * nothing found.
     *
     * @param event event object
     * @see GuiceyLifecycle#BundlesFromLookupResolved
     */
    protected void lookupBundlesResolved(final BundlesFromLookupResolvedEvent event) {
        // empty
    }

    /**
     * Called when all bundles are resolved (after dw recognition and lookup). Called even if no bundles
     * registered.
     *
     * @param event event object
     * @see GuiceyLifecycle#BundlesResolved
     */
    protected void bundlesResolved(final BundlesResolvedEvent event) {
        // empty
    }

    /**
     * Called after bundles initialization. Note that bundles could register other bundles and so resulted
     * list of installed bundles could be bigger (than in resolution event).
     *
     * @param event event object
     * @see GuiceyLifecycle#BundlesInitialized
     */
    protected void bundlesInitialized(final BundlesInitializedEvent event) {
        // empty
    }

    /**
     * Called if commands search is enabled ({@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#searchCommands()})
     * and at least one command found. Not called otherwise.
     *
     * @param event event object
     * @see GuiceyLifecycle#CommandsResolved
     */
    protected void commandsResolved(final CommandsResolvedEvent event) {
        // empty
    }

    /**
     * Called after all installers resolved (including installers found with classpath scan) and prepared for
     * processing extensions.
     *
     * @param event event object
     * @see GuiceyLifecycle#InstallersResolved
     */
    protected void installersResolved(final InstallersResolvedEvent event) {
        // empty
    }

    /**
     * Called when classes from classpath scan analyzed and all extensions detected. Called only if classpath scan
     * is enabled and at least one extension detected.
     *
     * @param event event object
     * @see GuiceyLifecycle#ClasspathExtensionsResolved
     */
    protected void classpathExtensionsResolved(final ClasspathExtensionsResolvedEvent event) {
        // empty
    }

    /**
     * Called after guicey initialization (includes bundles lookup and initialization,
     * installers and extensions resolution). Pure marker event, indicating guicey work finished under dropwizard
     * configuration phase.
     *
     * @param event event object
     * @see GuiceyLifecycle#Initialized
     */
    protected void initialized(final InitializedEvent event) {
        // empty
    }

    /**
     * Meta event. Called just before guice bundle processing in run phase.
     *
     * @param event event object
     * @see GuiceyLifecycle#BeforeRun
     */
    protected void beforeRun(final BeforeRunEvent event) {
        // empty
    }

    /**
     * Called after bundles start (run method call). Not called if no bundles were used at all.
     *
     * @param event event object
     * @see GuiceyLifecycle#BundlesStarted
     */
    protected void bundlesStarted(final BundlesStartedEvent event) {
        // empty
    }

    /**
     * Called when all manually registered extension classes are recognized by installers (validated). But only
     * extensions, known to be enabled at that time are actually validated (this way it is possible to exclude
     * extensions for non existing installers). Called only if at least one manual extension registered.
     *
     * @param event event object
     * @see GuiceyLifecycle#ManualExtensionsValidated
     */
    protected void manualExtensionsValidated(final ManualExtensionsValidatedEvent event) {
        // empty
    }

    /**
     * Called when guice bindings analyzed and all extensions detected. Called only if bindings analysis is enabled.
     *
     * @param event event object
     * @see GuiceyLifecycle#ModulesAnalyzed
     */
    protected void modulesAnalyzed(final ModulesAnalyzedEvent event) {
        // empty
    }

    /**
     * Called when all extensions detected (from classpath scan and guice modules). Called even if no extensions
     * configured to indicate configuration state.
     *
     * @param event event object
     * @see GuiceyLifecycle#ExtensionsResolved
     */
    protected void extensionsResolved(final ExtensionsResolvedEvent event) {
        // empty
    }

    /**
     * Called just before guice injector creation. Called even if no modules were used at all (to indicate major
     * lifecycle point).
     *
     * @param event event object
     * @see GuiceyLifecycle#InjectorCreation
     */
    protected void injectorCreation(final InjectorCreationEvent event) {
        // empty
    }

    /**
     * Called when installer installed all related extensions and only for installers actually performed
     * installations (extensions list never empty).
     *
     * @param event event object
     * @see GuiceyLifecycle#ExtensionsInstalledBy
     */
    protected void extensionsInstalledBy(final ExtensionsInstalledByEvent event) {
        // empty
    }

    /**
     * Called after all installers install related extensions. Not called when no extensions installed.
     *
     * @param event event object
     * @see GuiceyLifecycle#ExtensionsInstalled
     */
    protected void extensionsInstalled(final ExtensionsInstalledEvent event) {
        // empty
    }

    /**
     * Called after
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle#run(io.dropwizard.core.Configuration,
     * io.dropwizard.core.setup.Environment)}
     * when guicey context is started, extensions installed (but not hk extensions, because neither jersey nor jetty
     * isn't start yet).
     *
     * @param event event object
     * @see GuiceyLifecycle#ApplicationRun
     */
    protected void applicationRun(final ApplicationRunEvent event) {
        // empty
    }

    /**
     * Called after complete application configuration ({@link io.dropwizard.core.Application#run(
     * io.dropwizard.core.Configuration, io.dropwizard.core.setup.Environment)} called), but before lifecycle
     * startup (before managed objects run). Actually the same as jetty lifecycle started event
     * ({@link org.eclipse.jetty.util.component.LifeCycle.Listener#lifeCycleStarting(
     * org.eclipse.jetty.util.component.LifeCycle)}.
     *
     * @param event event object
     * @see GuiceyLifecycle#ApplicationStarting
     */
    protected void applicationStarting(final ApplicationStartingEvent event) {
        // empty
    }

    /**
     * Jersey context starting. At this point jersey and jetty is only initializing. Guicey jersey configuration
     * is not yer performed. Since that point jersey {@link org.glassfish.jersey.internal.inject.InjectionManager}
     * is accessible.
     *
     * @param event event object
     * @see GuiceyLifecycle#JerseyConfiguration
     */
    protected void jerseyConfiguration(final JerseyConfigurationEvent event) {
        // empty
    }

    /**
     * Called when {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} installer installed all
     * related extensions and only for installers actually performed installations (extensions list never empty).
     *
     * @param event event object
     * @see GuiceyLifecycle#JerseyExtensionsInstalledBy
     */
    protected void jerseyExtensionsInstalledBy(final JerseyExtensionsInstalledByEvent event) {
        // empty
    }

    /**
     * Called after all {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} installers install
     * related extensions and only when at least one extension was installed.
     *
     * @param event event object
     * @see GuiceyLifecycle#JerseyExtensionsInstalled
     */
    protected void jerseyExtensionsInstalled(final JerseyExtensionsInstalledEvent event) {
        // empty
    }

    /**
     * Called after complete dropwizard startup. Actually the same as jetty lifecycle started event (
     * {@link org.eclipse.jetty.util.component.LifeCycle.Listener#lifeCycleStarted(
     * org.eclipse.jetty.util.component.LifeCycle)}), which is called after complete jetty startup.
     *
     * @param event event object
     * @see GuiceyLifecycle#ApplicationStarted
     */
    protected void applicationStarted(final ApplicationStartedEvent event) {
        // empty
    }

    /**
     * Called on application shutdown start. Triggered by jetty lifecycle stopping event (
     * {@link org.eclipse.jetty.util.component.LifeCycle.Listener#lifeCycleStopping(
     * org.eclipse.jetty.util.component.LifeCycle)}).
     *
     * @param event event object
     * @see GuiceyLifecycle#ApplicationShutdown
     */
    protected void applicationShutdown(final ApplicationShutdownEvent event) {
        // empty
    }

    /**
     * Called after application shutdown. Triggered by jetty lifecycle stopping event (
     * {@link org.eclipse.jetty.util.component.LifeCycle.Listener#lifeCycleStopped(
     * org.eclipse.jetty.util.component.LifeCycle)}).
     *
     * @param event event object
     * @see GuiceyLifecycle#ApplicationStopped
     */
    protected void applicationStopped(final ApplicationStoppedEvent event) {
        // empty
    }
}
