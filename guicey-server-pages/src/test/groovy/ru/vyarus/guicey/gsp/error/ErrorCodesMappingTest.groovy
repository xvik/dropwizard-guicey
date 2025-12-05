package ru.vyarus.guicey.gsp.error

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.views.template.Template

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.Response

/**
 * @author Vyacheslav Rusakov
 * @since 24.01.2019
 */
@TestDropwizardApp(value = App, config = 'src/test/resources/conf.yml')
class ErrorCodesMappingTest extends AbstractTest {

    def "Check error mapping"() {

        when: "error processing template"
        def res = getHtml("/code/403")
        then: "error page"
        res.contains("Error code: 403")

        when: "error processing template"
        res = getHtml("/code/405")
        then: "error page"
        res.contains("Error code2: 405")

        when: "accessing not existing asset"
        getHtml("/notexisting.html")
        then: "no error mapped"
        thrown(FileNotFoundException)

        when: "accessing not existing template"
        getHtml("/notexisting.ftl")
        then: "no error mapped"
        thrown(FileNotFoundException)

        when: "error processing template"
        getHtml("/code/407")
        then: "no error mapped"
        thrown(IOException)
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(ErrorResource)
                    .bundles(
                            ServerPagesBundle.app("err", "/err", "/")
                                    .errorPage(403, "error.ftl")
                                    .errorPage(405, "error2.ftl")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Path('/err/')
    @Template
    public static class ErrorResource {

        @GET
        @Path("/code/{code}")
        public Response get(@PathParam("code") Integer code) {
            return Response.status(code).build()
        }
    }
}
