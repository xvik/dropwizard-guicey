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
class TestTemplateFailWithoutActivatedSupportTest extends AbstractTest {

    def "Check app resources access"() {

        when: "accessing html page"
        String res = getHtml("/app/")
        then: "resource found"
        res.contains("Sample ext page")

        when: "accessing direct resource (through servlet)"
        res = get("/app/some.css")
        then: "resource found"
        res.contains("external css ")

        when: "accessing template"
        getHtml("/app/template.ftl")
        then: "template rendering fails"
        def ex = thrown(IOException)
        ex.message == "status: 500"
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(ServerPagesBundle.builder()
                    //.enableFreemarkerCustomClassLoadersSupport()
                            .build(),
                            ServerPagesBundle.app("app", "extapp", "/app",
                                    new URLClassLoader([Paths.get("src/test/external").toUri().toURL()] as URL[]))
                                    .indexPage("index.html")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
