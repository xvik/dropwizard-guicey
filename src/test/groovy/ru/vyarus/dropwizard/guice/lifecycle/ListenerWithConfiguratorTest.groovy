package ru.vyarus.dropwizard.guice.lifecycle

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener
import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfiguratorsProcessedEvent
import ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2018
 */
@UseGuiceyApp(App)
class ListenerWithConfiguratorTest extends Specification {

    def "Check listener configurator support"() {

        expect: "both listeners used"
        Listener.calledConfigurators == 1
        ListenerInception.calledConfigurators == 1
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

    static class Listener implements GuiceyLifecycleListener, GuiceyConfigurator {

        static int calledConfigurators

        @Override
        void onEvent(GuiceyLifecycleEvent event) {
            if (event.getType() == GuiceyLifecycle.ConfiguratorsProcessed) {
                calledConfigurators = ((ConfiguratorsProcessedEvent) event).configurators.size()
            }
        }

        @Override
        void configure(GuiceBundle.Builder builder) {
            // listener register listener
            builder.listen(new ListenerInception())
        }
    }

    static class ListenerInception implements GuiceyLifecycleListener {
        static int calledConfigurators

        @Override
        void onEvent(GuiceyLifecycleEvent event) {
            if (event.getType() == GuiceyLifecycle.ConfiguratorsProcessed) {
                calledConfigurators = ((ConfiguratorsProcessedEvent) event).configurators.size()
            }
        }
    }
}