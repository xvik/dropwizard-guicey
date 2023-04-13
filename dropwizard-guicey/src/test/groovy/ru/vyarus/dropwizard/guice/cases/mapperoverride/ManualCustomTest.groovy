package ru.vyarus.dropwizard.guice.cases.mapperoverride

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.glassfish.jersey.internal.inject.Custom
import org.glassfish.jersey.internal.inject.InjectionManager
import org.glassfish.jersey.internal.inject.Providers
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.InstallersOptions
import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 04.10.2020
 */
@TestDropwizardApp(App)
class ManualCustomTest extends AbstractTest {

    @Inject
    InjectionManager manager

    def "Check default exception mapper override with manual custom"(ClientSupport client) {

        when: "Lookup mappers"
        def provs = Providers.getAllServiceHolders(manager, ExceptionMapper.class)
        then: "custom provider last"
        provs.first().contractTypes.contains(GeneralMapper)

        when: "Calling rest"
        Response res = client.targetRest('test').request().buildGet().invoke()
        then: "dw handler wins"
        res.status == 200
        GeneralMapper.handled
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
            // this should override default dropwizard handler because of manual @Custom
                    .extensions(GeneralMapper, SampleRest)
                    .printJerseyConfig()
                    .option(InstallersOptions.PrioritizeJerseyExtensions, false)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    @Provider
    @Custom
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
