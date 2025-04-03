package ru.vyarus.guicey.spa.err

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.guicey.spa.SpaBundle
import spock.lang.Specification
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension
import uk.org.webcompere.systemstubs.stream.SystemErr

/**
 * @author Vyacheslav Rusakov
 * @since 05.04.2017
 */
@ExtendWith(SystemStubsExtension)
class MappingClashTest extends Specification {

    @SystemStub
    SystemErr err

    def "Check uri paths clash"() {

        when: "starting app"
        Assertions.assertThrows(RuntimeException.class, () -> new App().run(['server'] as String[]))

        then: "error"
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

        @Override
        protected void onFatalError(Throwable t) {
            throw new RuntimeException(t)
        }
    }
}