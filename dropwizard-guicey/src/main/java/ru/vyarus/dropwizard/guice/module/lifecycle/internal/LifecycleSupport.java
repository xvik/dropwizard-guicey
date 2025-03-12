package ru.vyarus.dropwizard.guice.module.lifecycle.internal;

import com.google.common.base.Stopwatch;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.cli.Command;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.context.stat.DetailStat;
import ru.vyarus.dropwizard.guice.module.context.stat.Stat;
import ru.vyarus.dropwizard.guice.module.context.stat.StatTimer;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsTracker;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener;
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
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lifecycle broadcast internal support.
 *
 * @author Vyacheslav Rusakov
 * @since 17.04.2018
 */
@SuppressWarnings({"checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity",
        "PMD.ExcessiveImports", "PMD.TooManyMethods"})
public final class LifecycleSupport {
    private final Logger logger = LoggerFactory.getLogger(LifecycleSupport.class);

    private final StatsTracker tracker;
    private final EventsContext context;
    private final Runnable startupHook;
    private GuiceyLifecycle currentStage;

    private final Set<GuiceyLifecycleListener> listeners = new LinkedHashSet<>();

    public LifecycleSupport(final StatsTracker tracker, final Options options,
                            final SharedConfigurationState sharedState, final Runnable startupHook) {
        this.tracker = tracker;
        this.context = new EventsContext(tracker, options, sharedState);
        this.startupHook = startupHook;
    }

    public void register(final GuiceyLifecycleListener... listeners) {
        Arrays.asList(listeners).forEach(l -> {
            if (!this.listeners.add(l)) {
                logger.info("IGNORE duplicate lifecycle listener registration: {}", l.getClass().getName());
            }
        });
    }

    public void configurationHooksProcessed(final Set<GuiceyConfigurationHook> hooks) {
        if (hooks != null && !hooks.isEmpty()) {
            broadcast(new ConfigurationHooksProcessedEvent(context, hooks));
        }
    }

    public void beforeInit(final Bootstrap bootstrap) {
        this.context.setBootstrap(bootstrap);
        broadcast(new BeforeInitEvent(context));
    }

    public void dropwizardBundlesInitialized(final List<ConfiguredBundle> bundles,
                                             final List<ConfiguredBundle> disabled,
                                             final List<ConfiguredBundle> ignored) {
        if (!bundles.isEmpty()) {
            broadcast(new DropwizardBundlesInitializedEvent(context, bundles, disabled, ignored));
        }
    }

    public void bundlesFromLookupResolved(final List<GuiceyBundle> bundles) {
        if (!bundles.isEmpty()) {
            broadcast(new BundlesFromLookupResolvedEvent(context, bundles));
        }
    }

    public void bundlesResolved(final List<GuiceyBundle> bundles,
                                final List<GuiceyBundle> disabled,
                                final List<GuiceyBundle> ignored) {
        broadcast(new BundlesResolvedEvent(context, bundles, disabled, ignored));
    }

    public void bundlesInitialized(final List<GuiceyBundle> bundles,
                                   final List<GuiceyBundle> disabled,
                                   final List<GuiceyBundle> ignored) {
        if (!bundles.isEmpty()) {
            broadcast(new BundlesInitializedEvent(context, bundles, disabled, ignored));
        }
    }

    public void commandsResolved(final List<Command> installed) {
        if (installed != null && !installed.isEmpty()) {
            broadcast(new CommandsResolvedEvent(context, installed));
        }
    }

    public void installersResolved(final List<FeatureInstaller> installers,
                                   final List<Class<? extends FeatureInstaller>> disabled) {
        broadcast(new InstallersResolvedEvent(context, installers, disabled));
    }

    public void manualExtensionsValidated(final List<Class<?>> extensions, final List<Class<?>> validated) {
        if (!extensions.isEmpty()) {
            broadcast(new ManualExtensionsValidatedEvent(context, extensions, validated));
        }
    }

    public void classpathExtensionsResolved(final List<Class<?>> extensions) {
        if (!extensions.isEmpty()) {
            broadcast(new ClasspathExtensionsResolvedEvent(context, extensions));
        }
    }

    public void initialized() {
        broadcast(new InitializedEvent(context));
    }

    public void runPhase(final Configuration configuration,
                         final ConfigurationTree configurationTree,
                         final Environment environment) {
        this.context.setConfiguration(configuration);
        this.context.setConfigurationTree(configurationTree);
        this.context.setEnvironment(environment);
        broadcast(new BeforeRunEvent(context));
        // fire after complete initialization (final meta-event)
        environment.lifecycle().addEventListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStarted(final LifeCycle event) {
                applicationStarted();
            }

            @Override
            public void lifeCycleStopping(final LifeCycle event) {
                applicationShutdown();
            }

            @Override
            public void lifeCycleStopped(final LifeCycle event) {
                applicationStopped();
            }
        });
    }

    public void bundlesStarted(final List<GuiceyBundle> bundles) {
        if (!bundles.isEmpty()) {
            broadcast(new BundlesStartedEvent(context, bundles));
        }
    }

    public void modulesAnalyzed(final List<Module> modules,
                                final List<Class<?>> extensions,
                                final List<Class<? extends Module>> transitiveModulesRemoved,
                                final List<Binding> bindingsRemoved) {
        broadcast(new ModulesAnalyzedEvent(context, modules, extensions, transitiveModulesRemoved, bindingsRemoved));
    }

    public void extensionsResolved(final List<Class<?>> extensions, final List<Class<?>> disabled) {
        broadcast(new ExtensionsResolvedEvent(context, extensions, disabled));
    }

    public void injectorCreation(final List<Module> modules,
                                 final List<Module> overriding,
                                 final List<Module> disabled,
                                 final List<Module> ignored) {
        broadcast(new InjectorCreationEvent(context, modules, overriding, disabled, ignored));
    }

    public void injectorPhase(final Injector injector) {
        this.context.setInjector(injector);
    }

    public void extensionsInstalled(final Class<? extends FeatureInstaller> installer,
                                    final List<Class<?>> installed) {
        if (installed != null && !installed.isEmpty()) {
            broadcast(new ExtensionsInstalledByEvent(context, installer, installed));
        }
    }

    public void extensionsInstalled(final List<Class<?>> extensions) {
        if (!extensions.isEmpty()) {
            broadcast(new ExtensionsInstalledEvent(context, extensions));
        }
    }

    public void applicationRun() {
        broadcast(new ApplicationRunEvent(context));
    }


    public void jerseyConfiguration(final InjectionManager injectionManager) {
        this.context.setInjectionManager(injectionManager);
        broadcast(new JerseyConfigurationEvent(context));
    }


    public void jerseyExtensionsInstalled(final Class<? extends FeatureInstaller> installer,
                                          final List<Class<?>> installed) {
        if (installed != null && !installed.isEmpty()) {
            broadcast(new JerseyExtensionsInstalledByEvent(context, installer, installed));
        }
    }

    public void jerseyExtensionsInstalled(final List<Class<?>> extensions) {
        if (!extensions.isEmpty()) {
            broadcast(new JerseyExtensionsInstalledEvent(context, extensions));
        }
    }

    /**
     * @return current lifecycle phase
     */
    public GuiceyLifecycle getStage() {
        return currentStage;
    }

    private void broadcast(final GuiceyLifecycleEvent event) {
        if (!listeners.isEmpty()) {
            final StatTimer timer = tracker.timer(Stat.ListenersTime);
            final Stopwatch eventTimer = tracker.detailTimer(DetailStat.Listener, event.getClass());
            listeners.forEach(l -> l.onEvent(event));
            eventTimer.stop();
            timer.stop();
        }
        currentStage = event.getType();
    }

    private void applicationStarted() {
        broadcast(new ApplicationStartedEvent(context));
        startupHook.run();
    }

    private void applicationShutdown() {
        broadcast(new ApplicationShutdownEvent(context));
    }

    private void applicationStopped() {
        broadcast(new ApplicationStoppedEvent(context));
    }
}
