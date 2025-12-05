package ru.vyarus.guicey.gsp

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.support.app.SampleTemplateResource

/**
 * @author Vyacheslav Rusakov
 * @since 18.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class MultipleAppsMappingTest extends AbstractTest {

    def "Check app mapped"() {

        when: "accessing app"
        String res = getHtml("/app")
        then: "index page"
        res.contains("Sample page")

        when: "accessing direct file"
        res = getHtml("/app/index.html")
        then: "index page"
        res.contains("Sample page")

        when: "accessing resource"
        res = get("/app/css/style.css")
        then: "css"
        res.contains("/* sample page css */")

        when: "accessing direct template"
        res = getHtml("/app/template.ftl")
        then: "rendered template"
        res.contains("page: /app/template.ftl")

        when: "accessing template through resource"
        res = getHtml("/app/sample/tt")
        then: "template mapped"
        res.contains("name: tt")
    }

    def "Check app2 mapped"() {

        when: "accessing app"
        String res = getHtml("/app2")
        then: "index page"
        res.contains("Sample page")

        when: "accessing direct file"
        res = getHtml("/app2/index.html")
        then: "index page"
        res.contains("Sample page")

        when: "accessing resource"
        res = get("/app2/css/style.css")
        then: "css"
        res.contains("/* sample page css */")

        when: "accessing direct template"
        res = getHtml("/app2/template.ftl")
        then: "rendered template"
        res.contains("page: /app2/template.ftl")

        when: "accessing template through resource"
        res = getHtml("/app2/sample/tt")
        then: "template mapped"
        res.contains("name: tt")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(SampleTemplateResource)
                    .bundles(
                            ServerPagesBundle.app("app", "/app", "/app")
                                    .mapViews("app")
                                    .indexPage("index.html")
                                    .build(),
                            ServerPagesBundle.app("app2", "/app", "/app2")
                                    // use same rest as app
                                    .mapViews("app")
                                    .indexPage("index.html")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
