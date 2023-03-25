package ru.vyarus.guicey.gsp.classloader

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
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
class OverrideAssetsFromCustomLoaderTest extends AbstractTest {

    def "Check app resources access"() {

        when: "accessing html page"
        String res = getHtml("/app/")
        then: "page overridden"
        res.contains("Sample ext page")

        when: "accessing direct resource (through servlet)"
        res = get("/app/css/style.css")
        then: "resource overridden"
        res.contains("sample ext page css")
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
                            // overrides from custom classpath
                            ServerPagesBundle.extendApp('app',
                                    new URLClassLoader([Paths.get("src/test/external").toUri().toURL()] as URL[]))
                                    .attachAssets("extapp")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
