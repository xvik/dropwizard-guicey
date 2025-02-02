package ru.vyarus.dropwizard.guice.guiceconfig.prvt

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.PrivateModule
import com.google.inject.Singleton
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import io.dropwizard.lifecycle.Managed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Response
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 29.01.2025
 */
@TestDropwizardApp(App)
class IndirectWebExtensionsFromPrivateModuleTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector

    def "Check extensions registration form private binding"(ClientSupport client) {

        expect: "managed installed"
        info.getInfo(PrivateManaged) != null
        injector.getInstance(PrivateManaged).called

        and: "resource recognized"
        info.getInfo(PrivateResource) != null
        client.targetRest('/private').request().get().readEntity(String) == "{\"done=\": true}"

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new Module())
                    .printAllGuiceBindings()
                    .build()
            )
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Module extends PrivateModule {
        @Override
        protected void configure() {
            bind(Managed).to(PrivateManaged)
            expose(Managed)

            bind(PubRes).to(PrivateResource)
            expose(PubRes)
        }
    }

    @Singleton
    static class PrivateManaged implements Managed {
        private boolean called

        @Override
        void start() throws Exception {
            called = true
        }
    }

    static interface PubRes {}

    @Path("/private")
    @Produces('application/json')
    static class PrivateResource implements PubRes {
        @GET
        @Path("/")
        Response latest() {
            return Response.ok("{\"done=\": true}").build();
        }
    }
}
