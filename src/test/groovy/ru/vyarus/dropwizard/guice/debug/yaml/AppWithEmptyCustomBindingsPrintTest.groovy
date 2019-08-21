package ru.vyarus.dropwizard.guice.debug.yaml

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 13.06.2018
 */
@UseGuiceyApp(App)
class AppWithEmptyCustomBindingsPrintTest extends Specification {

    def "Check empty custom bindings print"() {

        expect:
        true
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .printCustomConfigurationBindings()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}
