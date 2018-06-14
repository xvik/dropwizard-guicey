package ru.vyarus.dropwizard.guice.module.lifecycle.internal;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.ServiceLocator;
import ru.vyarus.dropwizard.guice.configurator.ConfiguratorsSupport;
import ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfiguratorsProcessedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.InitializationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HkConfigurationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HkExtensionsInstalledByEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HkExtensionsInstalledEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.*;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

import java.util.*;

/**
 * Lifecycle broadcast internal support.
 *
 * @author Vyacheslav Rusakov
 * @since 17.04.2018
 */
@SuppressWarnings({"checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity"})
public final class LifecycleSupport {

    private final Options options;
    private Bootstrap bootstrap;
    private Configuration configuration;
    private ConfigurationTree configurationTree;
    private Environment environment;
    private Injector injector;
    private ServiceLocator locator;
    private GuiceyLifecycle currentStage;

    private final List<GuiceyLifecycleListener> listeners = new ArrayList<>();

    public LifecycleSupport(final Options options) {
        this.options = options;
    }

    public void register(final GuiceyLifecycleListener... listeners) {
        Arrays.asList(listeners).forEach(l -> {
            this.listeners.add(l);
            if (l instanceof GuiceyConfigurator) {
                Preconditions.checkState(isBefore(GuiceyLifecycle.ConfiguratorsProcessed),
                        "Can't register listener as configurator because configurators "
                                + "were already processed (current stage is %s).", currentStage);
                ConfiguratorsSupport.listen((GuiceyConfigurator) l);
            }
        });
    }

    public void configuratorsProcessed(final Set<GuiceyConfigurator> configurators) {
        if (configurators != null && !configurators.isEmpty()) {
            broadcast(new ConfiguratorsProcessedEvent(options, configurators));
        }
    }

    public void initialization(final Bootstrap bootstrap, final List<Command> installed) {
        broadcast(new InitializationEvent(options, bootstrap,
                installed != null ? installed : Collections.emptyList()));
        this.bootstrap = bootstrap;
    }

    public void runPhase(final Configuration configuration,
                         final ConfigurationTree configurationTree,
                         final Environment environment) {
        broadcast(new BeforeRunEvent(options, bootstrap, configuration, configurationTree, environment));
        this.configuration = configuration;
        this.configurationTree = configurationTree;
        this.environment = environment;
    }

    public void bundlesFromDwResolved(final List<GuiceyBundle> bundles) {
        if (!bundles.isEmpty()) {
            broadcast(new BundlesFromDwResolvedEvent(options, bootstrap,
                    configuration, configurationTree, environment, bundles));
        }
    }

    public void bundlesFromLookupResolved(final List<GuiceyBundle> bundles) {
        if (!bundles.isEmpty()) {
            broadcast(new BundlesFromLookupResolvedEvent(options, bootstrap,
                    configuration, configurationTree, environment, bundles));
        }
    }

    public void bundlesResolved(final List<GuiceyBundle> bundles, final List<GuiceyBundle> disabled) {
        broadcast(new BundlesResolvedEvent(options, bootstrap,
                configuration, configurationTree, environment, bundles, disabled));
    }

    public void bundlesProcessed(final List<GuiceyBundle> bundles, final List<GuiceyBundle> disabled) {
        if (!bundles.isEmpty()) {
            broadcast(new BundlesProcessedEvent(options, bootstrap,
                    configuration, configurationTree, environment, bundles, disabled));
        }
    }

    public void injectorCreation(final List<Module> modules, final List<Module> overriding,
                                 final List<Module> disabled) {
        broadcast(new InjectorCreationEvent(options, bootstrap,
                configuration, configurationTree, environment, modules, overriding, disabled));
    }

    public void installersResolved(final List<FeatureInstaller> installers,
                                   final List<Class<? extends FeatureInstaller>> disabled) {
        broadcast(new InstallersResolvedEvent(options, bootstrap,
                configuration, configurationTree, environment, installers, disabled));
    }

    public void extensionsResolved(final List<Class<?>> extensions, final List<Class<?>> disabled) {
        broadcast(new ExtensionsResolvedEvent(options, bootstrap,
                configuration, configurationTree, environment, extensions, disabled));
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


    public void hkConfiguration(final ServiceLocator locator) {
        broadcast(new HkConfigurationEvent(options, bootstrap,
                configuration, configurationTree, environment, injector, locator));
        this.locator = locator;
    }


    public void hkExtensionsInstalled(final Class<? extends FeatureInstaller> installer,
                                      final List<Class<?>> installed) {
        if (installed != null && !installed.isEmpty()) {
            broadcast(new HkExtensionsInstalledByEvent(options, bootstrap,
                    configuration, configurationTree, environment, injector, locator, installer, installed));
        }
    }

    public void hkExtensionsInstalled(final List<Class<?>> extensions) {
        if (!extensions.isEmpty()) {
            broadcast(new HkExtensionsInstalledEvent(options, bootstrap,
                    configuration, configurationTree, environment, injector, locator, extensions));
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
}
