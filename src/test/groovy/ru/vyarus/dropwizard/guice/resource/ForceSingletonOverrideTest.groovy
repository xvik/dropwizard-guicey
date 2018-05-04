package ru.vyarus.dropwizard.guice.resource

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.glassfish.hk2.api.PerLookup
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.ws.rs.GET
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2018
 */
@UseDropwizardApp(App)
class ForceSingletonOverrideTest extends AbstractTest {

    def "Check singleton override hk resource"() {

        when: "calling resource"
        new URL("http://localhost:8080/hk").getText()
        new URL("http://localhost:8080/hk").getText()

        then: "non singleton"
        Res.cnt == 2
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .useHK2ForJerseyExtensions()
                    .extensions(Res)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Path("/hk")
    @PerLookup  // annotation prevents forced singleton
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
}
