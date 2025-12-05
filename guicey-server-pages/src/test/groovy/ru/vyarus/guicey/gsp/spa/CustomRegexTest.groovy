package ru.vyarus.guicey.gsp.spa

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle

/**
 * @author Vyacheslav Rusakov
 * @since 18.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class CustomRegexTest extends AbstractTest {

    def "Check custom regex"() {

        when: "accessing html"
        String res = getHtml("/some/some.html")
        then: "index page"
        res.contains("Sample page")

        when: "accessing js"
        get("/some/some.js")
        then: "index page"
        thrown(FileNotFoundException)
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle
                                    .app("app", "/app", "/")
                                    .indexPage("index.html")
                                    .spaRouting("\\.js\$")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}