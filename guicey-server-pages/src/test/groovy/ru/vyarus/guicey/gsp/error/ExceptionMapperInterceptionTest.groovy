package ru.vyarus.guicey.gsp.error

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.views.template.ManualErrorHandling
import ru.vyarus.guicey.gsp.views.template.Template

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 09.06.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class ExceptionMapperInterceptionTest extends AbstractTest {

    def "Check error mapping"() {

        when: "accessing throwing resource"
        def res = getHtml("/err")
        then: "gsp error page"
        res == "Error: WebApplicationException"

        when: "accessing throwing resource with disabled error mechanism"
        res = getHtml("/err2")
        then: "manual error handling"
        res == "handled!"
    }


    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(ErrRest, ExHandler)
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("test.app", "/app", "/")
                                    .errorPage("error.ftl")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    @Path("/test.app/")
    @Template
    public static class ErrRest {

        @Path("/err")
        @GET
        public String get() {
            throw new IllegalArgumentException("Sample error")
        }

        @ManualErrorHandling
        @Path("/err2")
        @GET
        public String get2() {
            throw new IllegalArgumentException("Sample error")
        }
    }

    @Provider
    public static class ExHandler implements ExceptionMapper<IllegalArgumentException> {
        @Override
        Response toResponse(IllegalArgumentException exception) {
            return Response.ok("handled!").build()
        }
    }
}
