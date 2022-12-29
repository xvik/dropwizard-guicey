package ru.vyarus.dropwizard.guice.debug

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 09.12.2022
 */
@TestGuiceyApp(App)
class ExtensionsHelpTest extends Specification {

    def "Check extensions help"() {
        // actual reporting checked manually (test used for reporting configuration)

        expect: "checks that reporting doesn't fail"
        true
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .printExtensionsHelp()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
