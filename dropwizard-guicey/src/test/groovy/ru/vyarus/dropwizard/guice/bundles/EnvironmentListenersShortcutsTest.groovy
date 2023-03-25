package ru.vyarus.dropwizard.guice.bundles

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.lifecycle.Managed
import io.dropwizard.lifecycle.ServerLifecycleListener
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.component.LifeCycle
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 18.09.2019
 */
@TestDropwizardApp(App)
class EnvironmentListenersShortcutsTest extends Specification {

    def "Check listeners registration"() {

        expect: "listeners called"
        Mng.called
        LListener.called
        SListener.called
        Bundle.onGuiceyStartup
        Bundle.onStartup
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new Bundle())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Bundle implements GuiceyBundle {
        static boolean onStartup
        static boolean onGuiceyStartup

        @Override
        void run(GuiceyEnvironment environment) {
            environment.manage(new Mng())
            environment.listenJetty(new LListener())
            environment.listenServer(new SListener())
            environment.onGuiceyStartup({ cfg, env, inj ->
                onGuiceyStartup = true
                assert cfg != null
                assert env != null
                assert inj != null
            })
            environment.onApplicationStartup({ inj ->
                onStartup = true
                assert inj != null
            })
        }
    }

    static class Mng implements Managed {
        static boolean called

        @Override
        void start() throws Exception {
            called = true
        }

        @Override
        void stop() throws Exception {
        }
    }

    static class LListener implements LifeCycle.Listener {
        static boolean called

        @Override
        void lifeCycleStarted(LifeCycle event) {
            called = true
        }
    }

    static class SListener implements ServerLifecycleListener {
        static boolean called

        @Override
        void serverStarted(Server server) {
            called = true
        }
    }
}
