package ru.vyarus.dropwizard.guice.web

import com.google.inject.servlet.ServletScopes
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.inject.Inject
import javax.inject.Provider
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.core.UriInfo
import java.util.concurrent.CompletableFuture

/**
 * @author Vyacheslav Rusakov
 * @since 08.05.2018
 */
@UseDropwizardApp(App)
class GuiceRequestScopePropagationTest extends AbstractTest {

    def "Check filter works without guice module"() {

        expect: "all request injections work"
        new URL("http://localhost:8080/tt?q=1").getText() == "ok"
        SampleResource.value == "1"

        and: "other check request scoped objects not cached"
        new URL("http://localhost:8080/tt?q=2").getText() == "ok"
        SampleResource.value == "2"
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

    @Path("/tt")
    static class SampleResource {
        @Inject
        Provider<UriInfo> uri

        static String value

        @GET
        String ok() {
            uri.get().getRequestUri()

            // prepare request scope aware action for execution in other thread
            // UriInfo will be accessible only if its in request scope
            def action = ServletScopes.transferRequest {
                value = uri.get().getQueryParameters().getFirst("q")
            }
            CompletableFuture.runAsync {
                action.call()
            }.get()
            return "ok"
        }
    }
}
