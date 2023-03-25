package ru.vyarus.guicey.eventbus

import com.google.common.eventbus.Subscribe
import com.google.inject.AbstractModule
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guicey.eventbus.support.AbstractEvent
import ru.vyarus.guicey.eventbus.support.Event1
import ru.vyarus.guicey.eventbus.support.Event3
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 02.12.2016
 */
@TestGuiceyApp(App)
class ReportingLogTest extends Specification {

    def "Check correct registration"() {

        expect: "expecting log called"
        true

    }


    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new EventBusBundle())
                    // need to force listeners registration to actually call reporting to console
                    .modules(new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(SubscribersInfoTest.Service).asEagerSingleton()
                        }
                    })
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Service {

        @Subscribe
        void onEvent1(Event1 event) {
        }

        @Subscribe
        void onEvent3(Event3 event) {
        }

        @Subscribe
        void onEvent21(AbstractEvent event) {
        }
    }
}