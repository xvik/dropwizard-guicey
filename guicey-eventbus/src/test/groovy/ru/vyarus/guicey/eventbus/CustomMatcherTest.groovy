package ru.vyarus.guicey.eventbus

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.google.inject.matcher.Matchers
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guicey.eventbus.service.EventSubscribersInfo
import ru.vyarus.guicey.eventbus.support.AbstractEvent
import ru.vyarus.guicey.eventbus.support.Event1
import ru.vyarus.guicey.eventbus.support.Event2
import ru.vyarus.guicey.eventbus.support.Event3
import ru.vyarus.guicey.eventbus.support.HasEvents
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 02.12.2016
 */
@TestGuiceyApp(App.class)
class CustomMatcherTest extends Specification {
    @Inject
    EventBus bus
    @Inject
    Service service // trigger JIT binding
    @Inject
    ServiceNoEvents serviceNoEvents // trigger JIT binding
    @Inject
    EventSubscribersInfo info

    def "Check correct registration"() {

        expect: "listeners registered"
        info.getListenedEvents() == [Event1] as Set
        info.getListenerTypes(Event1) == [Service] as Set
        info.getListenerTypes(Event2).isEmpty()
        info.getListenerTypes(Event3).isEmpty()
        info.getListenerTypes(AbstractEvent).isEmpty()

    }

    def "Check publication"() {

        when: "publish event"
        bus.post(new Event1())
        then: "received"
        service.event1 == 1
        serviceNoEvents.event1 == 0
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new EventBusBundle().withMatcher(Matchers.annotatedWith(HasEvents)))
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @HasEvents
    static class Service {

        int event1

        @Subscribe
        void onEvent1(Event1 event) {
            event1++
        }
    }

    static class ServiceNoEvents {

        int event1

        @Subscribe
        void onEvent1(Event1 event) {
            event1++
        }
    }
}