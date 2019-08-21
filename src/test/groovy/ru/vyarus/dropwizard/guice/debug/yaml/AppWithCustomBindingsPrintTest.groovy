package ru.vyarus.dropwizard.guice.debug.yaml

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import ru.vyarus.dropwizard.guice.yaml.support.ComplexGenericCase
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov
 * @since 13.06.2018
 */
@UseGuiceyApp(App)
class AppWithCustomBindingsPrintTest extends Specification {

    def "Check custom bindings print"() {

        expect:
        true
    }

    static class App extends Application<ComplexGenericCase> {

        @Override
        void initialize(Bootstrap<ComplexGenericCase> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .printCustomConfigurationBindings()
                    .build())
        }

        @Override
        void run(ComplexGenericCase configuration, Environment environment) throws Exception {

        }
    }
}