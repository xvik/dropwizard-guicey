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
 * @since 21.01.2019
 */
@TestDropwizardApp(value = App, config = 'src/test/resources/conf.yml')
class AdminErrorMappingTest extends AbstractTest {

    def "Check error mapping"() {

        when: "accessing not existing asset"
        def res = adminGetHtml("/appp/notexisting.html")
        then: "error page"
        res.contains("custom error page")

        when: "accessing not existing template"
        res = adminGetHtml("/appp/notexisting.ftl")
        then: "error page"
        res.contains("custom error page")

        when: "accessing not existing path"
        res = adminGetHtml("/appp/notexisting/")
        then: "error page"
        res.contains("custom error page")

        when: "error processing template"
        res = adminGetHtml("/appp/sample/error")
        then: "error page"
        res.contains("custom error page")

        when: "error processing template"
        res = adminGetHtml("/appp/sample/error2")
        then: "error page"
        res.contains("custom error page")

        when: "direct 404 rest response"
        res = adminGetHtml("/appp/sample/notfound")
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
                            ServerPagesBundle.adminApp("app", "/app", "/appp")
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
