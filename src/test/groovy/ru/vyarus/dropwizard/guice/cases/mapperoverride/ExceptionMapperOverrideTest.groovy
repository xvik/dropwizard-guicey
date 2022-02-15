package ru.vyarus.dropwizard.guice.cases.mapperoverride

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.glassfish.jersey.internal.inject.InjectionManager
import org.glassfish.jersey.internal.inject.Providers
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 03.10.2020
 */
@TestDropwizardApp(App)
class ExceptionMapperOverrideTest extends AbstractTest {

    @Inject
    InjectionManager manager

    def "Check default exception mapper override"(ClientSupport client) {

        when: "Lookup mappers"
        def provs = Providers.getAllServiceHolders(manager, ExceptionMapper.class)
        then: "custom provider first"
        provs[0].contractTypes.contains(GeneralMapper)

        when: "Calling rest"
        Response res = client.targetRest('test').request().buildGet().invoke()
        then:
        res.status == 200
        GeneralMapper.handled
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
            // this should override default dropwizard handler because it should be bound as @Custom
                    .extensions(GeneralMapper, SampleRest)
                    .printJerseyConfig()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    @Provider
    static class GeneralMapper implements ExceptionMapper<Throwable> {

        static boolean handled

        @Override
        Response toResponse(Throwable exception) {
            handled = true
            return Response.ok().build();
        }
    }

    @Path("/test")
    static class SampleRest {

        @GET
        void test() {
            throw new IllegalArgumentException("Should be handled by mapper")
        }
    }
}
