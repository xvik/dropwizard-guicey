package ru.vyarus.dropwizard.guice.module.lifecycle.internal;

import com.google.inject.Injector;
import com.google.inject.Module;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.ServiceLocator;
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfiguratorsProcessedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.InitializationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HkConfigurationEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HkExtensionsInstalledByEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.hk.HkExtensionsInstalledEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.*;
import ru.vyarus.dropwizard.guice.module.support.conf.ConfiguratorsSupport;
import ru.vyarus.dropwizard.guice.module.support.conf.GuiceyConfigurator;

import java.util.*;

/**
 * Lifecycle broadcast internal support.
 *
 * @author Vyacheslav Rusakov
 * @since 17.04.2018
 */
@SuppressWarnings({"checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity"})
public final class LifecycleSupport {

    private final OptionsInfo options;
    private Bootstrap bootstrap;
    private Configuration configuration;
    private Environment environment;
    private Injector injector;
    private ServiceLocator locator;

    private final List<GuiceyLifecycleListener> listeners = new ArrayList<>();

    public LifecycleSupport(final OptionsInfo options) {
        this.options = options;
    }

    public void register(final GuiceyLifecycleListener... listeners) {
        Arrays.asList(listeners).forEach(l -> {
            this.listeners.add(l);
            if (l instanceof GuiceyConfigurator) {
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

    public void runPhase(final Configuration configuration, final Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    public void bundlesFromDwResolved(final List<GuiceyBundle> bundles) {
        if (!bundles.isEmpty()) {
            broadcast(new BundlesFromDwResolvedEvent(options, bootstrap, configuration, environment, bundles));
        }
    }

    public void bundlesFromLookupResolved(final List<GuiceyBundle> bundles) {
        if (!bundles.isEmpty()) {
            broadcast(new BundlesFromLookupResolvedEvent(options, bootstrap, configuration, environment, bundles));
        }
    }

    public void bundlesResolved(final List<GuiceyBundle> bundles, final List<GuiceyBundle> disabled) {
        broadcast(new BundlesResolvedEvent(options, bootstrap, configuration, environment,
                bundles, disabled));
    }

    public void bundlesProcessed(final List<GuiceyBundle> bundles, final List<GuiceyBundle> disabled) {
        if (!bundles.isEmpty()) {
            broadcast(new BundlesProcessedEvent(options, bootstrap, configuration, environment,
                    bundles, disabled));
        }
    }

    public void injectorCreation(final List<Module> modules, final List<Module> overriding,
                                 final List<Module> disabled) {
        broadcast(new InjectorCreationEvent(options, bootstrap, configuration, environment,
                modules, overriding, disabled));
    }

    public void installersResolved(final List<FeatureInstaller> installers,
                                   final List<Class<? extends FeatureInstaller>> disabled) {
        broadcast(new InstallersResolvedEvent(options, bootstrap, configuration, environment,
                installers, disabled));
    }

    public void extensionsResolved(final List<Class<?>> extensions, final List<Class<?>> disabled) {
        broadcast(new ExtensionsResolvedEvent(options, bootstrap, configuration, environment,
                extensions, disabled));
    }

    public void injectorPhase(final Injector injector) {
        this.injector = injector;
    }

    public void extensionsInstalled(final Class<? extends FeatureInstaller> installer,
                                    final List<Class<?>> installed) {
        if (installed != null && !installed.isEmpty()) {
            broadcast(new ExtensionsInstalledByEvent(options, bootstrap, configuration, environment, injector,
                    installer, installed));
        }
    }

    public void extensionsInstalled(final List<Class<?>> extensions) {
        if (!extensions.isEmpty()) {
            broadcast(new ExtensionsInstalledEvent(options, bootstrap, configuration, environment,
                    injector, extensions));
        }
    }

    public void applicationRun() {
        broadcast(new ApplicationRunEvent(options, bootstrap, configuration, environment, injector));
    }


    public void hkConfiguration(final ServiceLocator locator) {
        broadcast(new HkConfigurationEvent(options, bootstrap, configuration, environment, injector, locator));
        this.locator = locator;
    }


    public void hkExtensionsInstalled(final Class<? extends FeatureInstaller> installer,
                                      final List<Class<?>> installed) {
        if (installed != null && !installed.isEmpty()) {
            broadcast(new HkExtensionsInstalledByEvent(options, bootstrap, configuration, environment,
                    injector, locator, installer, installed));
        }
    }

    public void hkExtensionsInstalled(final List<Class<?>> extensions) {
        if (!extensions.isEmpty()) {
            broadcast(new HkExtensionsInstalledEvent(options, bootstrap, configuration, environment,
                    injector, locator, extensions));
        }
    }

    private void broadcast(final GuiceyLifecycleEvent event) {
        listeners.forEach(l -> l.onEvent(event));
    }
}
