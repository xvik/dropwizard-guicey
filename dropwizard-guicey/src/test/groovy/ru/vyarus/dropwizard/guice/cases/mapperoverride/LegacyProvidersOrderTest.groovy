package ru.vyarus.dropwizard.guice.cases.mapperoverride

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.glassfish.jersey.internal.inject.InjectionManager
import org.glassfish.jersey.internal.inject.Providers
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.InstallersOptions
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
 * @since 04.10.2020
 */
@TestDropwizardApp(App)
class LegacyProvidersOrderTest extends AbstractTest {

    @Inject
    InjectionManager manager

    def "Check default exception mapper not overridden"(ClientSupport client) {

        when: "Lookup mappers"
        def provs = Providers.getAllServiceHolders(manager, ExceptionMapper.class)
        then: "custom provider last"
        // jersey's ValidationExceptionMapper may be last or before last
        provs.last().contractTypes.contains(GeneralMapper) || provs[provs.size() - 2].contractTypes.contains(GeneralMapper)

        when: "Calling rest"
        Response res = client.targetRest('test').request().buildGet().invoke()
        then: "dw handler wins"
        res.status == 500
        !GeneralMapper.handled
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
            // this should NOT override default dropwizard handler because auto @Custom disabled
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
            throw new IllegalArgumentException("Should NOT be handled by mapper")
        }
    }
}
