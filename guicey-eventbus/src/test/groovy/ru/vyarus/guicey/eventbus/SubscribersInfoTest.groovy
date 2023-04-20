package ru.vyarus.guicey.eventbus

import com.google.common.eventbus.Subscribe
import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.dropwizard.guice.test.jupiter.param.Jit
import ru.vyarus.guicey.eventbus.service.EventSubscribersInfo
import ru.vyarus.guicey.eventbus.support.AbstractEvent
import ru.vyarus.guicey.eventbus.support.Event1
import ru.vyarus.guicey.eventbus.support.Event2
import ru.vyarus.guicey.eventbus.support.Event3
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 02.12.2016
 */
@TestGuiceyApp(App)
class SubscribersInfoTest extends Specification {

    // can't use @Inject here because it will not work on shared fields
    // but shared state is important because there are two tests
    @Shared
    Service service
    @Shared
    Service2 service2
    @Inject
    EventSubscribersInfo info
    @Inject
    Injector injector

    // trigger JIT binding
    void setupSpec(@Jit Service service, @Jit Service2 service2) {
        this.service = service
        this.service2 = service2
    }

    def "Check correct tracking"() {

        expect: "listeners registered"
        info.getListenedEvents() == [Event1, Event3, AbstractEvent] as Set
        info.getListeners(Event1) == [service, service2] as Set
        info.getListeners(Event2).isEmpty()
        info.getListeners(Event3) == [service] as Set
        info.getListeners(AbstractEvent) == [service] as Set

    }

    def "Check multiple instances"() {

        when: "create multiply instances (prototype scope)"
        Service2 inst1 = injector.getInstance(Service2)
        Service2 inst2 = injector.getInstance(Service2)

        then: "instances tracked"
        info.getListeners(Event1) == [service, service2, inst1, inst2] as Set
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

    static class Service2 {

        @Subscribe
        void onEvent1(Event1 event) {
        }
    }
}