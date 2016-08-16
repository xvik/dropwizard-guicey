package ru.vyarus.dropwizard.guice.config.debug

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 17.08.2016
 */
@UseGuiceyApp(AIApp)
class AvailableInstallersPrintTest extends Specification {

    def "Check available installers reporting"() {

        // actual reporting checked manually (test used for reporting configuration)

        expect: "checks that reporting doesn't fail"
        true

    }

    static class AIApp extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .useWebInstallers()
                    .printAvailableInstallers()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}