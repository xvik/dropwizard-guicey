package ru.vyarus.guicey.gsp.admin

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
 * @since 21.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class AdminMappingTest extends AbstractTest {

    def "Check app mapped"() {

        when: "accessing app"
        String res = adminGetHtml("/appp/")
        then: "index page"
        res.contains("Sample page")

        when: "accessing direct file"
        res = adminGetHtml("/appp/index.html")
        then: "index page"
        res.contains("Sample page")

        when: "accessing resource"
        res = adminGet("/appp/css/style.css")
        then: "css"
        res.contains("/* sample page css */")

        when: "accessing direct template"
        res = adminGetHtml("/appp/template.ftl")
        then: "rendered template"
        res.contains("page: /appp/template.ftl")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.adminApp("app", "/app", "/appp/")
                                    .indexPage("index.html")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
