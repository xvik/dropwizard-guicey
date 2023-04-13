package ru.vyarus.dropwizard.guice.web.noguice

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import jakarta.inject.Inject
import jakarta.inject.Provider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.SecurityContext

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2016
 */
@TestDropwizardApp(NFApp)
class NoGuiceFilterTest extends AbstractTest {

    def "Check filter works without guice module"() {

        expect: "all request injections work"
        new URL("http://localhost:8080/tt").getText() == "ok"
    }

    static class NFApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .noGuiceFilter()
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
        Provider<HttpServletRequest> request
        @Inject
        Provider<HttpServletResponse> response
        @Inject
        Provider<SecurityContext> security

        @GET
        String ok() {
            assert request.get().requestURI == "/tt"
            assert response.get() != null
            assert !security.get().secure
            return "ok"
        }
    }
}