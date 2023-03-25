package ru.vyarus.dropwizard.guice.config

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener
import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 26.04.2018
 */
@TestGuiceyApp(App)
class BundleLifecycleListenerTest extends AbstractTest {

    def "Check bundle listener used"() {

        expect: "listener called"
        XListener.events.size() > 1
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new GuiceyBundle() {
                        @Override
                        void initialize(GuiceyBootstrap gbootstrap) {
                            gbootstrap.listen(new XListener())
                        }
                    }).build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class XListener implements GuiceyLifecycleListener {
        static List<GuiceyLifecycle> events = new ArrayList<>()

        @Override
        void onEvent(GuiceyLifecycleEvent event) {
            events.add(event.getType())
        }
    }
}
