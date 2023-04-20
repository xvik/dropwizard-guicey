package ru.vyarus.guicey.validation

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject
import javax.validation.ConstraintViolationException
import javax.validation.constraints.NotNull
import javax.validation.executable.ValidateOnExecution

/**
 * @author Vyacheslav Rusakov
 * @since 30.12.2019
 */
@TestGuiceyApp(App)
class ExplicitValidationTest extends Specification {

    @Inject
    Service service

    def "Check implicit validation enabled"() {

        when: "call method with incorrect parameter"
        service.call(null)
        then: "no validation because no explicit activator"
        true

        when: "call annotated method with incorrect parameter"
        service.call2(null)
        then: "validation applied"
        thrown(ConstraintViolationException)
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new ValidationBundle()
                            .validateAnnotatedOnly())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Service {

        public void call(@NotNull Object arg) {

        }

        @ValidateOnExecution
        public void call2(@NotNull Object arg) {

        }
    }
}
