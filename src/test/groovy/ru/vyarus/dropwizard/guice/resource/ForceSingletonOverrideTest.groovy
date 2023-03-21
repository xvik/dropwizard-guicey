package ru.vyarus.dropwizard.guice.resource

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged
import ru.vyarus.dropwizard.guice.module.support.scope.Prototype
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import javax.ws.rs.GET
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2018
 */
@TestDropwizardApp(App)
class ForceSingletonOverrideTest extends AbstractTest {

    def "Check singleton override hk resource"() {

        when: "calling resource"
        new URL("http://localhost:8080/hk").getText()
        new URL("http://localhost:8080/hk").getText()

        new URL("http://localhost:8080/guice").getText()
        new URL("http://localhost:8080/guice").getText()

        then: "non singleton"
        Res.cnt == 2
        GuiceRes.cnt == 2
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(Res, GuiceRes)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Path("/hk")
    @JerseyManaged
    //@PerLookup
    // annotation prevents forced singleton
    static class Res {

        static int cnt = 0

        Res() {
            cnt++
        }

        @Path("/")
        @GET
        def smth() {
            ''
        }
    }

    @Path("/guice")
    @Prototype
    // annotation prevents forced singleton
    static class GuiceRes {

        static int cnt = 0

        GuiceRes() {
            cnt++
        }

        @Path("/")
        @GET
        def smth() {
            ''
        }
    }
}
