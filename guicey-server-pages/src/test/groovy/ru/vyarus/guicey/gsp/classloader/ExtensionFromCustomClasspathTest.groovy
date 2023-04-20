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
 * @since 09.04.2020
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class ExtensionFromCustomClasspathTest extends AbstractTest {

    def "Check app resources access"() {

        when: "accessing html page"
        String res = getHtml("/app/")
        then: "resource found"
        res.contains("Sample page")

        when: "accessing external asset"
        res = getHtml("/app/ext.ftl")
        then: "resource found"
        res.contains("external template")

        when: "accessing direct resource (through servlet)"
        res = getHtml("/app/other.css")
        then: "resource found"
        res.contains("other css")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(ServerPagesBundle.builder()
                            .enableFreemarkerCustomClassLoadersSupport()
                            .build(),
                            // app resources from classpath
                            ServerPagesBundle.app("app", "app", "/app")
                                    .indexPage("index.html")
                                    .build(),
                            // external assets
                            ServerPagesBundle.extendApp('app',
                                    new URLClassLoader([Paths.get("src/test/external").toUri().toURL()] as URL[]))
                                    .attachAssets("extra")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
