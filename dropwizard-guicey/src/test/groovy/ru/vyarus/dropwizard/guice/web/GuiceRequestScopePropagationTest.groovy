package ru.vyarus.dropwizard.guice.web

import com.google.inject.servlet.ServletScopes
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import jakarta.inject.Inject
import jakarta.inject.Provider
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.UriInfo
import java.util.concurrent.CompletableFuture

/**
 * @author Vyacheslav Rusakov
 * @since 08.05.2018
 */
@TestDropwizardApp(App)
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
            // jersey object must be resolved inside hk request scope (to store it in guice request scope)
            // so guice could see its instance later in another thread
            uri.get()

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
