package ru.vyarus.guicey.gsp.views

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.ServerPagesBundle
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 07.02.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class PrintEmptyConfigurationTest extends Specification {

    def "Check empty config printing"() {

        expect: "created main config empty map correctlu prointed"
        true
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder()
                                    .viewsConfiguration({ null })
                                    .printViewsConfiguration()
                                    .build(),
                            ServerPagesBundle.app("app", "/app", "/").build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
