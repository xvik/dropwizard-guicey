package ru.vyarus.dropwizard.guice.module.lifecycle.internal;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.*;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.JerseyConfigurationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.JerseyExtensionsInstalledByEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.JerseyExtensionsInstalledEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.*;
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

    private final EventsContext context;
    private GuiceyLifecycle currentStage;

    private final Set<GuiceyLifecycleListener> listeners = new LinkedHashSet<>();

    public LifecycleSupport(final Options options, final SharedConfigurationState sharedState) {
        this.context = new EventsContext(options, sharedState);
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

    public void initializationStarted(final Bootstrap bootstrap,
                                      final List<ConfiguredBundle> bundles,
                                      final List<ConfiguredBundle> disabled,
                                      final List<ConfiguredBundle> ignored) {
        this.context.setBootstrap(bootstrap);
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
        environment.lifecycle().addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarted(final LifeCycle event) {
                applicationStarted();
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
        listeners.forEach(l -> l.onEvent(event));
        currentStage = event.getType();
    }

    private void applicationStarted() {
        broadcast(new ApplicationStartedEvent(context));
    }
}
