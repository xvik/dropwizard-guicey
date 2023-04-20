package ru.vyarus.guicey.spa

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 05.04.2017
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class CustomIndexTest extends AbstractTest {

    def "Check custom index"() {

        when: "accessing app"
        String res = get("/")
        then: "index page"
        res.contains("Other index")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(SpaBundle
                            .app("app", "/app", "/")
                            .indexPage("idx.htm")
                            .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}