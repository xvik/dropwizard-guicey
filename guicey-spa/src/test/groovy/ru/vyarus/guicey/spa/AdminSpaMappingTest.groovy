package ru.vyarus.guicey.spa

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 05.04.2017
 */
@TestDropwizardApp(App)
class AdminSpaMappingTest extends AbstractTest {

    def "Check spa mapped"() {

        when: "accessing app"
        String res = adminGet("/app")
        then: "index page"
        res.contains("Sample page")

        when: "accessing not existing page"
        res = adminGet("/app/some")
        then: "error"
        res.contains("Sample page")

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(SpaBundle.adminApp("app", "/app", "/app").build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}