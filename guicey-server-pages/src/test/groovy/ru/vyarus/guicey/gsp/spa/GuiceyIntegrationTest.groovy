package ru.vyarus.guicey.gsp.spa

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle

/**
 * @author Vyacheslav Rusakov
 * @since 18.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class GuiceyIntegrationTest extends AbstractTest {

    def "Check spa mapped"() {

        when: "accessing app"
        String res = getHtml("/")
        then: "index page"
        res.contains("Sample page")

        when: "accessing not existing page"
        res = getHtml("/some/")
        then: "ok"
        res.contains("Sample page")

        when: "accessing not existing resource"
        getHtml("/some.html")
        then: "error"
        thrown(FileNotFoundException)
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(ServerPagesBundle.builder().build(),
                            new AppBundle())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class AppBundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.bundles(
                    ServerPagesBundle
                            .app('app', '/app', '/')
                            .indexPage('index.html')
                            .spaRouting()
                            .build()
            )
        }
    }
}