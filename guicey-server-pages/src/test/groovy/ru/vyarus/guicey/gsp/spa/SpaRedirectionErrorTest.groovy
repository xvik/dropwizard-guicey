package ru.vyarus.guicey.gsp.spa

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle

/**
 * @author Vyacheslav Rusakov
 * @since 06.02.2019
 */
@TestDropwizardApp(value = App, config = 'src/test/resources/conf.yml')
class SpaRedirectionErrorTest extends AbstractTest {

    def "Check spa mapped"() {

        when: "accessing not existing page"
        def res = getHtml("/some/")
        then: "error page instead of index"
        res.contains("custom error page")

        when: "accessing not existing resource"
        res = getHtml("/some.html")
        then: "error page instead of index"
        res.contains("custom error page")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/")
                            // bad index page
                                    .indexPage("/sample/error")
                                    .errorPage("error.html")
                                    .spaRouting()
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
