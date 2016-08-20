package ru.vyarus.dropwizard.guice.web.noguice

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.inject.Inject
import javax.inject.Provider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.core.SecurityContext

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2016
 */
@UseDropwizardApp(NFApp)
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