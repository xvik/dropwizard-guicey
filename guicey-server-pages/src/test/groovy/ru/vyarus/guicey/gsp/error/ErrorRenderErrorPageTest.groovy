package ru.vyarus.guicey.gsp.error

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.support.app.SampleTemplateResource

/**
 * @author Vyacheslav Rusakov
 * @since 30.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class ErrorRenderErrorPageTest extends AbstractTest {

    def "Check error mapping"() {

        when: "accessing not existing asset"
        getHtml("/notexisting.html")
        then: "error page failed to render"
        thrown(FileNotFoundException)

        when: "accessing not existing template"
        getHtml("/notexisting.ftl")
        then: "error page failed to render"
        thrown(FileNotFoundException)

        when: "accessing not existing path"
        getHtml("/notexisting/")
        then: "error page failed to render"
        thrown(FileNotFoundException)

        when: "error processing template"
        getHtml("/sample/error")
        then: "error page failed to render (500)"
        thrown(IOException)

        when: "error processing template"
        getHtml("/sample/error2")
        then: "error page failed to render (500)"
        thrown(IOException)

        when: "direct 404 rest response"
        getHtml("/sample/notfound")
        then: "error page failed to render"
        thrown(FileNotFoundException)
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(SampleTemplateResource)
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/")
                                    .errorPage("/sample/error")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
