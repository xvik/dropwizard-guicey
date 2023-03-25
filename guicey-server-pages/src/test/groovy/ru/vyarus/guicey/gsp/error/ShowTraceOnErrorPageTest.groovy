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
 * @since 29.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class ShowTraceOnErrorPageTest extends AbstractTest {

    def "Check error mapping"() {

        when: "accessing not existing asset"
        def res = getHtml("/notexisting.html")
        then: "error page"
        res.startsWith("ru.vyarus.guicey.gsp.app.filter.AssetError: Error serving asset /notexisting.html: 404")

        when: "accessing not existing template"
        res = getHtml("/notexisting.ftl")
        then: "error page"
        res.startsWith("javax.ws.rs.NotFoundException: Template 'notexisting.ftl' not found")

        when: "accessing not existing path"
        res = getHtml("/notexisting/")
        then: "error page"
        res.startsWith("javax.ws.rs.NotFoundException: HTTP 404 Not Found")

        when: "error processing template"
        res = getHtml("/sample/error")
        then: "error page"
        res.startsWith("javax.ws.rs.WebApplicationException: HTTP 500 Internal Server Error")

        when: "error processing template"
        res = getHtml("/sample/error2")
        then: "error page"
        res.startsWith("javax.ws.rs.WebApplicationException: error")

        when: "direct 404 rest response"
        res = getHtml("/sample/notfound")
        then: "error page"
        res.startsWith("ru.vyarus.guicey.gsp.app.rest.support.TemplateRestCodeError: Error processing template rest call app/sample/notfound: 404")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(SampleTemplateResource)
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/")
                                    .errorPage("error2.ftl")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
