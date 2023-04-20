package ru.vyarus.guicey.gsp.error


import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.ServerPagesBundle
import spock.lang.Specification

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType

/**
 * @author Vyacheslav Rusakov
 * @since 27.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class NonErrorInterceptionTest extends Specification {

    def "Check non error forwarding"(ClientSupport client) {

        when: "calling for non 200 response"
        def res = client.targetMain('/res').request(MediaType.TEXT_HTML).get()
        then: "redirect"
        res.status == 304

        when: "direct rest non 200 return"
        res = client.targetMain('/res/2').request(MediaType.TEXT_HTML).get()
        then: "redirect"
        res.status == 304
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(Resource)
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/")
                                    .errorPage("error.html")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Path("/app/res")
    static class Resource {

        @GET
        @Path("/")
        void get() {
            throw new WebApplicationException(304)
        }


        @GET
        @Path("/2")
        javax.ws.rs.core.Response get2() {
            return javax.ws.rs.core.Response.status(304).build()
        }
    }
}
