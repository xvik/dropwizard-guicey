package ru.vyarus.dropwizard.guice.config

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.junit.runners.model.Statement
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.config.BundleListenerRegistrationFailTest.XListener
import ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener
import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule

/**
 * @author Vyacheslav Rusakov
 * @since 26.04.2018
 */
class BundleListenerRegistrationFailTest extends AbstractTest {

    def "Check listener registration fail"() {

        when: "starting app"
        new GuiceyAppRule(App, null).apply({} as Statement, null).evaluate()
        then: "fail"
        def ex = thrown(IllegalStateException)
        ex.cause.message.startsWith("Can't register listener as configurator because configurators")

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

    static class XListener implements GuiceyLifecycleListener, GuiceyConfigurator {
        @Override
        void configure(GuiceBundle.Builder builder) {

        }

        @Override
        void onEvent(GuiceyLifecycleEvent event) {

        }
    }
}
