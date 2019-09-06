package ru.vyarus.dropwizard.guice.module.lifecycle.internal;

import com.google.common.base.Preconditions;
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
@SuppressWarnings({"checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity"})
public final class LifecycleSupport {
    private final Logger logger = LoggerFactory.getLogger(LifecycleSupport.class);

    private final Options options;
    private Bootstrap bootstrap;
    private Configuration configuration;
    private ConfigurationTree configurationTree;
    private Environment environment;
    private Injector injector;
    private InjectionManager injectionManager;
    private GuiceyLifecycle currentStage;

    private final Set<GuiceyLifecycleListener> listeners = new LinkedHashSet<>();

    public LifecycleSupport(final Options options) {
        this.options = options;
    }

    public void register(final GuiceyLifecycleListener... listeners) {
        Arrays.asList(listeners).forEach(l -> {
            if (!this.listeners.add(l)) {
                logger.info("IGNORE duplicate lifecycle listener registration: {}", l.getClass().getName());
            }
            if (l instanceof GuiceyConfigurationHook) {
                Preconditions.checkState(isBefore(GuiceyLifecycle.ConfigurationHooksProcessed),
                        "Can't register listener as hook because hooks "
                                + "were already processed (current stage is %s).", currentStage);
                ((GuiceyConfigurationHook) l).register();
            }
        });
    }

    public void configurationHooksProcessed(final Set<GuiceyConfigurationHook> hooks) {
        if (hooks != null && !hooks.isEmpty()) {
            broadcast(new ConfigurationHooksProcessedEvent(options, hooks));
        }
    }

    public void initializationStarted(final Bootstrap bootstrap,
                                      final List<ConfiguredBundle> bundles,
                                      final List<ConfiguredBundle> disabled,
                                      final List<ConfiguredBundle> ignored) {
        this.bootstrap = bootstrap;
        if (!bundles.isEmpty()) {
            broadcast(new DropwizardBundlesInitializedEvent(options, bootstrap, bundles, disabled, ignored));
        }
    }

    public void bundlesFromLookupResolved(final List<GuiceyBundle> bundles) {
        if (!bundles.isEmpty()) {
            broadcast(new BundlesFromLookupResolvedEvent(options, bootstrap, bundles));
        }
    }

    public void bundlesResolved(final List<GuiceyBundle> bundles,
                                final List<GuiceyBundle> disabled,
                                final List<GuiceyBundle> ignored) {
        broadcast(new BundlesResolvedEvent(options, bootstrap, bundles, disabled, ignored));
    }

    public void bundlesInitialized(final List<GuiceyBundle> bundles,
                                   final List<GuiceyBundle> disabled,
                                   final List<GuiceyBundle> ignored) {
        if (!bundles.isEmpty()) {
            broadcast(new BundlesInitializedEvent(options, bootstrap, bundles, disabled, ignored));
        }
    }

    public void commandsResolved(final List<Command> installed) {
        if (installed != null && !installed.isEmpty()) {
            broadcast(new CommandsResolvedEvent(options, bootstrap, installed));
        }
    }

    public void installersResolved(final List<FeatureInstaller> installers,
                                   final List<Class<? extends FeatureInstaller>> disabled) {
        broadcast(new InstallersResolvedEvent(options, bootstrap, installers, disabled));
    }

    public void manualExtensionsValidated(final List<Class<?>> extensions, final List<Class<?>> validated) {
        if (!extensions.isEmpty()) {
            broadcast(new ManualExtensionsValidatedEvent(options, bootstrap, extensions, validated));
        }
    }

    public void classpathExtensionsResolved(final List<Class<?>> extensions) {
        if (!extensions.isEmpty()) {
            broadcast(new ClasspathExtensionsResolvedEvent(options, bootstrap, extensions));
        }
    }

    public void initialized() {
        broadcast(new InitializedEvent(options, bootstrap));
    }

    public void runPhase(final Configuration configuration,
                         final ConfigurationTree configurationTree,
                         final Environment environment) {
        broadcast(new BeforeRunEvent(options, bootstrap, configuration, configurationTree, environment));
        this.configuration = configuration;
        this.configurationTree = configurationTree;
        this.environment = environment;
        // fire after complete initialization (final meta-event)
        environment.lifecycle().addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarted(LifeCycle event) {
                applicationStarted();
            }
        });
    }

    public void bundlesStarted(final List<GuiceyBundle> bundles) {
        if (!bundles.isEmpty()) {
            broadcast(new BundlesStartedEvent(options, bootstrap,
                    configuration, configurationTree, environment, bundles));
        }
    }

    public void modulesAnalyzed(final List<Module> modules,
                                final List<Class<?>> extensions,
                                final List<Class<? extends Module>> transitiveModulesRemoved,
                                final List<Binding> bindingsRemoved) {
        broadcast(new ModulesAnalyzedEvent(options, bootstrap,
                configuration, configurationTree, environment,
                modules, extensions, transitiveModulesRemoved, bindingsRemoved));
    }

    public void extensionsResolved(final List<Class<?>> extensions, final List<Class<?>> disabled) {
        broadcast(new ExtensionsResolvedEvent(options, bootstrap,
                configuration, configurationTree, environment, extensions, disabled));
    }

    public void injectorCreation(final List<Module> modules,
                                 final List<Module> overriding,
                                 final List<Module> disabled,
                                 final List<Module> ignored) {
        broadcast(new InjectorCreationEvent(options, bootstrap,
                configuration, configurationTree, environment, modules, overriding, disabled, ignored));
    }

    public void injectorPhase(final Injector injector) {
        this.injector = injector;
    }

    public void extensionsInstalled(final Class<? extends FeatureInstaller> installer,
                                    final List<Class<?>> installed) {
        if (installed != null && !installed.isEmpty()) {
            broadcast(new ExtensionsInstalledByEvent(options, bootstrap,
                    configuration, configurationTree, environment, injector, installer, installed));
        }
    }

    public void extensionsInstalled(final List<Class<?>> extensions) {
        if (!extensions.isEmpty()) {
            broadcast(new ExtensionsInstalledEvent(options, bootstrap,
                    configuration, configurationTree, environment, injector, extensions));
        }
    }

    public void applicationRun() {
        broadcast(new ApplicationRunEvent(options, bootstrap,
                configuration, configurationTree, environment, injector));
    }


    public void jerseyConfiguration(final InjectionManager injectionManager) {
        broadcast(new JerseyConfigurationEvent(options, bootstrap,
                configuration, configurationTree, environment, injector, injectionManager));
        this.injectionManager = injectionManager;
    }


    public void jerseyExtensionsInstalled(final Class<? extends FeatureInstaller> installer,
                                          final List<Class<?>> installed) {
        if (installed != null && !installed.isEmpty()) {
            broadcast(new JerseyExtensionsInstalledByEvent(options, bootstrap,
                    configuration, configurationTree, environment, injector, injectionManager, installer, installed));
        }
    }

    public void jerseyExtensionsInstalled(final List<Class<?>> extensions) {
        if (!extensions.isEmpty()) {
            broadcast(new JerseyExtensionsInstalledEvent(options, bootstrap,
                    configuration, configurationTree, environment, injector, injectionManager, extensions));
        }
    }

    /**
     * @return current lifecycle phase
     */
    public GuiceyLifecycle getStage() {
        return currentStage;
    }

    /**
     * @param lifecycle target lifecycle stage
     * @return true if current lifecycle is before provided stage, false otherwise
     */
    public boolean isBefore(final GuiceyLifecycle lifecycle) {
        return getStage() == null || getStage().ordinal() < lifecycle.ordinal();
    }

    private void broadcast(final GuiceyLifecycleEvent event) {
        listeners.forEach(l -> l.onEvent(event));
        currentStage = event.getType();
    }

    private void applicationStarted() {
        broadcast(new ApplicationStartedEvent(options, bootstrap,
                configuration, configurationTree, environment, injector, injectionManager));
    }
}
