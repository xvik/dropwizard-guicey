package ru.vyarus.dropwizard.guice.resource

import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.glassfish.jersey.server.ContainerRequest
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider
import org.glassfish.jersey.server.internal.process.AsyncContext
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.Request
import javax.ws.rs.core.SecurityContext
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.Providers

/**
 * @author Vyacheslav Rusakov
 * @since 29.03.2017
 */
@UseDropwizardApp(App)
class JerseyBindingsTest extends AbstractTest {

    def "Check jersey bindings"() {

        expect: "bindings ok inside request"
        new URL("http://localhost:8080/sample/").getText() == 'ok'
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(SampleResource)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Path("/sample")
    static class SampleResource {
        @Inject
        Injector injector

        @GET
        String request() {
            [MultivaluedParameterExtractorProvider,
             javax.ws.rs.core.Application,
             Providers,
             UriInfo,
             ResourceInfo,
             HttpHeaders,
             SecurityContext,
             Request,
             ContainerRequest,
             AsyncContext].each {
                assert injector.getInstance(it) != null
            }

            return "ok"
        }
    }
}