package ru.vyarus.guicey.gsp.ext

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
 * @since 11.06.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class WebjarsIntegrationTest extends Specification {

    def "Check webjars binding"() {

        when: "accessing jquery script"
        def res = new URL("http://localhost:8080/jquery/3.4.1/dist/jquery.min.js").text
        then: "ok"
        res.contains("jQuery v3.4.1")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/")
                                    .attachWebjars()
                                    .build())
                    .build())

        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
