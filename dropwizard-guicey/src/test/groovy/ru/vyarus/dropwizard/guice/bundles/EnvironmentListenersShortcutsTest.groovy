package ru.vyarus.dropwizard.guice.bundles

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import io.dropwizard.lifecycle.Managed
import io.dropwizard.lifecycle.ServerLifecycleListener
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.component.LifeCycle
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ru.vyarus.dropwizard.guice.AbstractPlatformTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 18.09.2019
 */
class EnvironmentListenersShortcutsTest extends AbstractPlatformTest {

    @Test
    void testListeners() {

        when: "run test"
        run(Test1)
        when: "listeners called"
        Mng.start
        Mng.stop
        LListener.start
        LListener.stop
        SListener.start
        Bundle.onGuiceyStartup
        Bundle.onStartup
        Bundle.onShutdown
    }

    @TestDropwizardApp(App)
    @Disabled
    static class Test1 {

        @Test
        void test() {
            Assertions.assertTrue(Mng.start)
            Assertions.assertTrue(LListener.start)
            Assertions.assertTrue(SListener.start)
            Assertions.assertTrue(Bundle.onGuiceyStartup)
            Assertions.assertTrue(Bundle.onStartup)
        }
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
        static boolean onShutdown
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
            environment.onApplicationShutdown ({ inj ->
                onShutdown = true
                assert inj != null
            })
        }
    }

    static class Mng implements Managed {
        static boolean start
        static boolean stop

        @Override
        void start() throws Exception {
            start = true
        }

        @Override
        void stop() throws Exception {
            stop = true
        }
    }

    static class LListener implements LifeCycle.Listener {
        static boolean start
        static boolean stop

        @Override
        void lifeCycleStarted(LifeCycle event) {
            start = true
        }

        @Override
        void lifeCycleStopped(LifeCycle event) {
            stop = true
        }
    }

    static class SListener implements ServerLifecycleListener {
        static boolean start

        @Override
        void serverStarted(Server server) {
            start = true
        }
    }

    @Override
    protected String clean(String out) {
        return out
    }
}
