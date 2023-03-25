package ru.vyarus.guicey.eventbus

import com.google.common.eventbus.AsyncEventBus
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guicey.eventbus.service.EventSubscribersInfo
import ru.vyarus.guicey.eventbus.support.Event1
import ru.vyarus.guicey.eventbus.support.Event2
import ru.vyarus.guicey.eventbus.support.HasEvents
import spock.lang.Specification

import javax.inject.Inject
import java.util.concurrent.Executors

/**
 * @author Vyacheslav Rusakov
 * @since 02.12.2016
 */
@TestGuiceyApp(App)
class CustomBusTest extends Specification {

    @Inject
    EventBus bus
    @Inject
    Service service // trigger JIT binding
    @Inject
    EventSubscribersInfo info

    def "Check correct registration"() {

        expect: "listeners registered"
        bus instanceof AsyncEventBus
        info.getListenedEvents() == [Event1] as Set
        info.getListenerTypes(Event1) == [Service] as Set
        info.getListenerTypes(Event2).isEmpty()

    }

    def "Check publication"() {

        when: "publish event"
        bus.post(new Event1())
        sleep(100)
        then: "received"
        service.event1 == 1
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new EventBusBundle(new AsyncEventBus(Executors.newSingleThreadExecutor())))
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
}
