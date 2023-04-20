package ru.vyarus.guicey.gsp.classloader

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle

import java.nio.file.Paths

/**
 * @author Vyacheslav Rusakov
 * @since 14.04.2020
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class ErrorPageFromCustomLoaderTest extends AbstractTest {

    def "Check error page"() {

        when: "accessing not existing asset"
        def res = getHtml("/notexisting.html")
        then: "error page"
        res.contains("Error: AssetError")

        when: "accessing not existing template"
        res = getHtml("/notexisting.ftl")
        then: "error page"
        res.contains("Error: NotFoundException")

        when: "accessing not existing path"
        res = getHtml("/notexisting/")
        then: "error page"
        res.contains("Error: NotFoundException")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(ServerPagesBundle.builder()
                            .enableFreemarkerCustomClassLoadersSupport()
                            .build(),
                            ServerPagesBundle.app("app", "extapp", "/",
                                    new URLClassLoader([Paths.get("src/test/external").toUri().toURL()] as URL[]))
                                    .errorPage("error.ftl")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
