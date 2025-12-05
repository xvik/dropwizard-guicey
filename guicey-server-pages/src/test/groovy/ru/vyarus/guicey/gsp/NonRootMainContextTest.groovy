package ru.vyarus.guicey.gsp

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 21.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*",
        configOverride = "server.applicationContextPath: /prefix/")
class NonRootMainContextTest extends AbstractTest {

    def "Check app mapped"() {

        when: "accessing app"
        String res = getHtml("/prefix/")
        then: "index page"
        res.contains("Sample page")

        when: "accessing direct file"
        res = getHtml("/prefix/index.html")
        then: "index page"
        res.contains("Sample page")

        when: "accessing resource"
        res = get("/prefix/css/style.css")
        then: "css"
        res.contains("/* sample page css */")

        when: "accessing direct template"
        res = getHtml("/prefix/template.ftl")
        then: "rendered template"
        res.contains("page: /prefix/template.ftl")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.app("app", "/app", "/")
                                    .indexPage("index.html")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
