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
@TestDropwizardApp(value = App, restMapping = "/rest/*",
        configOverride = [
                "server.applicationContextPath: /prefix",
                "server.adminContextPath: /admin"])
class ComplexFlatMappingTest extends AbstractTest {

    def "Check app mapped"() {

        when: "accessing app"
        String res = adminGetHtml("/admin/ap/")
        then: "index page"
        res.contains("Sample page")

        when: "accessing direct file"
        res = adminGetHtml("/admin/ap/index.html")
        then: "index page"
        res.contains("Sample page")

        when: "accessing resource"
        res = adminGet("/admin/ap/css/style.css")
        then: "css"
        res.contains("/* sample page css */")

        when: "accessing direct template"
        res = adminGetHtml("/admin/ap/template.ftl")
        then: "rendered template"
        res.contains("page: /admin/ap/template.ftl")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.adminApp("app", "/app", "/ap")
                                    .indexPage("index.html")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
