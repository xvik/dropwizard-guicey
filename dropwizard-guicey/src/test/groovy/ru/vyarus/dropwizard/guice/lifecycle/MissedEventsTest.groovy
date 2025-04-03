package ru.vyarus.dropwizard.guice.lifecycle

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener
import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfigurationHooksProcessedEvent
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2018
 */
@TestGuiceyApp(value = App, useDefaultExtensions = false)
class MissedEventsTest extends Specification {

    def "Check missed events"() {

        expect: "only few events called"
        Listener.events.contains(GuiceyLifecycle.Initialized)
        Listener.events.contains(GuiceyLifecycle.BundlesResolved)
        Listener.events.contains(GuiceyLifecycle.InjectorCreation)
        Listener.events.contains(GuiceyLifecycle.InstallersResolved)
        Listener.events.contains(GuiceyLifecycle.ExtensionsResolved)
        Listener.events.contains(GuiceyLifecycle.ApplicationRun)


        and: "not called"
        !Listener.events.contains(GuiceyLifecycle.ConfigurationHooksProcessed)
        !Listener.events.contains(GuiceyLifecycle.BundlesFromLookupResolved)
        !Listener.events.contains(GuiceyLifecycle.BundlesInitialized)
        !Listener.events.contains(GuiceyLifecycle.CommandsResolved)
        !Listener.events.contains(GuiceyLifecycle.BundlesStarted)
        !Listener.events.contains(GuiceyLifecycle.ExtensionsInstalledBy)
        !Listener.events.contains(GuiceyLifecycle.ExtensionsInstalled)
        !Listener.events.contains(GuiceyLifecycle.JerseyConfiguration)
        !Listener.events.contains(GuiceyLifecycle.JerseyExtensionsInstalledBy)
        !Listener.events.contains(GuiceyLifecycle.JerseyExtensionsInstalled)
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .listen(new Listener())
                    .noDefaultInstallers()
                    .printLifecyclePhases()
                    .disableBundleLookup()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    static class Listener implements GuiceyLifecycleListener {

        static List<GuiceyLifecycle> events = new ArrayList<>()

        @Override
        void onEvent(GuiceyLifecycleEvent event) {
            if (!events.contains(event.getType())) {
                events.add(event.getType())
            }
            if (event.getType() == GuiceyLifecycle.ConfigurationHooksProcessed) {
                throw new IllegalStateException("Hooks used!")
            }
            if (event.getType() == GuiceyLifecycle.BundlesFromLookupResolved) {
                throw new IllegalStateException("Bundle lookup used!")
            }
        }
    }
}