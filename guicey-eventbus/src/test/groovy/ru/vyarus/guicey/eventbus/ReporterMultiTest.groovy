package ru.vyarus.guicey.eventbus

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guicey.eventbus.report.EventSubscribersReporter
import ru.vyarus.guicey.eventbus.service.EventSubscribersInfo
import ru.vyarus.guicey.eventbus.support.AbstractEvent
import ru.vyarus.guicey.eventbus.support.Event1
import ru.vyarus.guicey.eventbus.support.Event3
import ru.vyarus.guicey.eventbus.support.HasEvents
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 02.12.2016
 */
@TestGuiceyApp(App)
class ReporterMultiTest extends Specification {
    @Inject
    EventBus bus
    @Inject
    Service service // trigger JIT binding
    @Inject
    EventSubscribersInfo info
    EventSubscribersReporter reporter

    void setup() {
        reporter = new EventSubscribersReporter(info)
    }

    def "Check print"() {

        expect: "reported"
        reporter.renderReport().replaceAll("\r", "") == """EventBus subscribers = 

    AbstractEvent
        ru.vyarus.guicey.eventbus.ReporterMultiTest\$Service

    Event1
        ru.vyarus.guicey.eventbus.ReporterMultiTest\$Service

    Event3
        ru.vyarus.guicey.eventbus.ReporterMultiTest\$Service
"""

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new EventBusBundle().noReport())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @HasEvents
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