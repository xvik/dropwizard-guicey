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
 * @since 23.01.2019
 */
@TestDropwizardApp(value = App, config = 'src/test/resources/conf.yml')
class ErrorTemplateTest extends AbstractTest {

    def "Check error mapping"() {

        when: "accessing not existing asset"
        def res = getHtml("/notexisting.html")
        then: "error page"
        res.contains("Error: AssetError")

        when: "accessing not existing template"
        res = getHtml("/notexisting.ftl")
        then: "error page"
        res.contains("Error: NotFoundException")

        when: "accessing not existing path"
        res = getHtml("/notexisting/")
        then: "error page"
        res.contains("Error: NotFoundException")

        when: "error processing template"
        res = getHtml("/sample/error")
        then: "error page"
        res.contains("Error: WebApplicationException")

        when: "error processing template"
        res = getHtml("/sample/error2")
        then: "error page"
        res.contains("Error: WebApplicationException")

        when: "direct 404 rest response"
        res = getHtml("/sample/notfound")
        then: "error page"
        res.contains("Error: TemplateRestCodeError")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(SampleTemplateResource)
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/")
                                    .errorPage("error.ftl")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
