package ru.vyarus.guicey.eventbus

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guicey.eventbus.service.EventSubscribersInfo
import ru.vyarus.guicey.eventbus.support.AbstractEvent
import ru.vyarus.guicey.eventbus.support.Event1
import ru.vyarus.guicey.eventbus.support.Event2
import ru.vyarus.guicey.eventbus.support.Event3
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 02.12.2016
 */
@TestGuiceyApp(App.class)
class SubscriptionTest extends Specification {

    @Inject
    EventBus bus
    @Inject
    Service service // trigger JIT binding
    @Inject
    EventSubscribersInfo info

    def "Check correct registration"() {

        expect: "listeners registered"
        info.getListenedEvents() == [Event1, Event3, AbstractEvent] as Set
        info.getListenerTypes(Event1) == [Service] as Set
        info.getListenerTypes(Event2).isEmpty()
        info.getListenerTypes(Event3) == [Service] as Set
        info.getListenerTypes(AbstractEvent) == [Service] as Set

    }

    def "Check publication"() {

        when: "publish first event"
        bus.post(new Event1())
        then: "received"
        service.event1 == 1
        service.event3 == 0
        service.event21 == 1

        when: "publish second event"
        bus.post(new Event2())
        then: "received"
        service.event1 == 1
        service.event3 == 0
        service.event21 == 2

        when: "publish third event"
        bus.post(new Event3())
        then: "received"
        service.event1 == 1
        service.event3 == 1
        service.event21 == 2
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new EventBusBundle())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Service {

        int event1
        int event3
        int event21

        @Subscribe
        void onEvent1(Event1 event) {
            event1++
        }

        @Subscribe
        void onEvent3(Event3 event) {
            event3++
        }

        @Subscribe
        void onEvent21(AbstractEvent event) {
            event21++
        }
    }
}