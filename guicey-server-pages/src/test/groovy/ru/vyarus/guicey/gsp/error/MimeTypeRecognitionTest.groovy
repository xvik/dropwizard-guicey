package ru.vyarus.guicey.gsp.error


import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.support.app.SampleTemplateResource
import spock.lang.Specification

import javax.ws.rs.core.MediaType

/**
 * @author Vyacheslav Rusakov
 * @since 27.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class MimeTypeRecognitionTest extends Specification {

    def "Check error mapping"(ClientSupport client) {

        when: "accessing not existing asset"
        def res = client.targetMain('/notexisting.html').request(MediaType.TEXT_HTML).get()
        then: "error page"
        res.readEntity(String) == "custom error page"

        when: "accessing not existing asset with text result"
        res = client.targetMain('/notexisting.html').request(MediaType.TEXT_PLAIN).get()
        then: "no error page"
        res.status == 404


        when: "accessing not existing template"
        res = client.targetMain('/notexisting.ftl').request(MediaType.TEXT_HTML).get()
        then: "error page"
        res.readEntity(String) == "custom error page"

        when: "accessing not existing template with text result"
        res = client.targetMain('/notexisting.ftl').request(MediaType.TEXT_PLAIN).get()
        then: "no error page"
        res.status == 404


        when: "accessing not existing path"
        res = client.targetMain('/notexisting/').request(MediaType.TEXT_HTML).get()
        then: "error page"
        res.readEntity(String) == "custom error page"

        when: "accessing not existing path with text result"
        res = client.targetMain('/notexisting/').request(MediaType.TEXT_PLAIN).get()
        then: "no error page"
        res.status == 404


        when: "error processing template"
        res = client.targetMain('/sample/error').request(MediaType.TEXT_HTML).get()
        then: "error page"
        res.readEntity(String) == "custom error page"

        when: "error processing template with text result"
        res = client.targetMain('/sample/error').request(MediaType.TEXT_PLAIN).get()
        then: "no error page"
        res.status == 500


        when: "error processing template"
        res = client.targetMain('/sample/error2').request(MediaType.TEXT_HTML).get()
        then: "error page"
        res.readEntity(String) == "custom error page"

        when: "error processing template with text result"
        res = client.targetMain('/sample/error2').request(MediaType.TEXT_PLAIN).get()
        then: "no error page"
        res.status == 500


        when: "direct 404 rest response"
        res = client.targetMain('/sample/notfound').request(MediaType.TEXT_HTML).get()
        then: "error page"
        res.readEntity(String) == "custom error page"

        when: "direct 404 rest response with text result"
        res = client.targetMain('/sample/notfound').request(MediaType.TEXT_PLAIN).get()
        then: "error page"
        res.status == 404
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