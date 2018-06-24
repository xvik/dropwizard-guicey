package ru.vyarus.dropwizard.guice.lifecycle

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener
import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfigurationHooksProcessedEvent
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2018
 */
@UseGuiceyApp(App)
class ListenerWithHookTest extends Specification {

    def "Check listener hooks support"() {

        expect: "both listeners used"
        Listener.calledHooks == 1
        ListenerInception.calledHooks == 1
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .listen(new Listener())
                    .printLifecyclePhasesDetailed()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Listener implements GuiceyLifecycleListener, GuiceyConfigurationHook {

        static int calledHooks

        @Override
        void onEvent(GuiceyLifecycleEvent event) {
            if (event.getType() == GuiceyLifecycle.ConfigurationHooksProcessed) {
                calledHooks = ((ConfigurationHooksProcessedEvent) event).hooks.size()
            }
        }

        @Override
        void configure(GuiceBundle.Builder builder) {
            // listener register listener
            builder.listen(new ListenerInception())
        }
    }

    static class ListenerInception implements GuiceyLifecycleListener {
        static int calledHooks

        @Override
        void onEvent(GuiceyLifecycleEvent event) {
            if (event.getType() == GuiceyLifecycle.ConfigurationHooksProcessed) {
                calledHooks = ((ConfigurationHooksProcessedEvent) event).hooks.size()
            }
        }
    }
}