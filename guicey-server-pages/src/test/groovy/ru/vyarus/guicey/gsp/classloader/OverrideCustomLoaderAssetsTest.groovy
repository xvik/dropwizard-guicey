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
 * @since 13.04.2020
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class OverrideCustomLoaderAssetsTest extends AbstractTest {
    // override custom class loader assets with assets from app class loader

    def "Check app resources access"() {

        when: "accessing html page"
        String res = getHtml("/app/")
        then: "page overridden"
        res.contains("Sample page")

        when: "accessing direct resource (through servlet)"
        res = get("/app/css/style.css")
        then: "resource overridden"
        res.contains("sample page css")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(ServerPagesBundle.builder()
                            .enableFreemarkerCustomClassLoadersSupport()
                            .build(),
                            // app resources from custom loader
                            ServerPagesBundle.app("app", "extapp", "/app",
                                    new URLClassLoader([Paths.get("src/test/external").toUri().toURL()] as URL[]))
                                    .indexPage("index.html")
                                    .build(),
                            // overrides from application classpath
                            ServerPagesBundle.extendApp('app')
                                    .attachAssets("app")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
