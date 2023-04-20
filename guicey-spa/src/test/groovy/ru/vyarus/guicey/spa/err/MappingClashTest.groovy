package ru.vyarus.guicey.spa.err

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.junit.jupiter.api.extension.ExtendWith
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.guicey.spa.SpaBundle
import spock.lang.Specification
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension
import uk.org.webcompere.systemstubs.security.SystemExit
import uk.org.webcompere.systemstubs.stream.SystemErr

/**
 * @author Vyacheslav Rusakov
 * @since 05.04.2017
 */
@ExtendWith(SystemStubsExtension)
class MappingClashTest extends Specification {

    @SystemStub
    SystemExit exit
    @SystemStub
    SystemErr err

    def "Check uri paths clash"() {

        when: "starting app"
        exit.execute(() -> {
            new App().run(['server'] as String[])
        });

        then: "error"
        exit.exitCode == 1
        err.text.contains("Assets servlet app2 registration clash with already installed servlets on paths: /app/*")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            SpaBundle.app("app1", "/app", "/app").build(),
                            SpaBundle.app("app2", "/app", "/app").build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}