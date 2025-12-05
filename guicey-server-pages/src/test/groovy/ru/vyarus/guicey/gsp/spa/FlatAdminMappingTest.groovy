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
@TestDropwizardApp(value = App, config = 'src/test/resources/flat.yml')
class FlatAdminMappingTest extends AbstractTest {

    def "Check spa mapped"() {

        when: "accessing app"
        String res = getHtml("/admin/app")
        then: "index page"
        res.contains("Sample page")

        when: "accessing not existing page"
        res = getHtml("/admin/app/some")
        then: "index page"
        res.contains("Sample page")

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle
                                    .adminApp("app", "/app", "/app")
                                    .indexPage("index.html")
                                    .spaRouting()
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}