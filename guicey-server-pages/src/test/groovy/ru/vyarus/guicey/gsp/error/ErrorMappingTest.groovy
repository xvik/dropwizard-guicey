package ru.vyarus.guicey.gsp.error

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.support.app.SampleTemplateResource

/**
 * @author Vyacheslav Rusakov
 * @since 15.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class ErrorMappingTest extends AbstractTest {

    def "Check error mapping"() {

        when: "accessing not existing asset"
        def res = getHtml("/notexisting.html")
        then: "error page"
        res.contains("custom error page")

        when: "accessing not existing template"
        res = getHtml("/notexisting.ftl")
        then: "error page"
        res.contains("custom error page")

        when: "accessing not existing path"
        res = getHtml("/notexisting/")
        then: "error page"
        res.contains("custom error page")

        when: "error processing template"
        res = getHtml("/sample/error")
        then: "error page"
        res.contains("custom error page")

        when: "error processing template"
        res = getHtml("/sample/error2")
        then: "error page"
        res.contains("custom error page")

        when: "direct 404 rest response"
        res = getHtml("/sample/notfound")
        then: "error page"
        res.contains("custom error page")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(SampleTemplateResource)
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/")
                                    .indexPage("index.html")
                                    .errorPage("error.html")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
