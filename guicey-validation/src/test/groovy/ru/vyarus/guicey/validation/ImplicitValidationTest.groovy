package ru.vyarus.guicey.validation

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject
import jakarta.validation.ConstraintViolationException
import jakarta.validation.constraints.NotNull

/**
 * @author Vyacheslav Rusakov
 * @since 30.12.2019
 */
@TestGuiceyApp(App)
class ImplicitValidationTest extends Specification {

    @Inject
    Service service

    def "Check implicit validation enabled"() {

        when: "call service with incorrect parameter"
        service.call(null)
        then: "validation failed"
        thrown(ConstraintViolationException)

        when: "call with correct param"
        service.call(12)
        then: "ok"
        true
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Service {

        public void call(@NotNull Object arg) {

        }
    }
}
