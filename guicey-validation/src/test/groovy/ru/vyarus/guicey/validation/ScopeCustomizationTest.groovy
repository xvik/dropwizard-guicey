package ru.vyarus.guicey.validation

import com.google.inject.matcher.Matchers
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * @author Vyacheslav Rusakov
 * @since 30.12.2019
 */
@TestGuiceyApp(App)
class ScopeCustomizationTest extends Specification {

    @Inject
    Service service

    @Inject
    Service2 service2

    def "Check implicit validation enabled"() {

        when: "service 1 ignored"
        service.call(null)
        service.call2(null)
        then: "no validation because no explicit activator"
        true

        when: "service 2 simple method ignored"
        service2.call(null)
        then: "no validation"
        true

        when: "service 2 annotated method checked"
        service2.call2(null)
        then: "validation applied"
        thrown(ConstraintViolationException)
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new ValidationBundle()
                    // avoid service 1
                            .targetClasses(Matchers.not(Matchers.subclassesOf(Service.class)))
                    // validate only methods with @Valid
                    // NOTE explicit mode is not enabled! its pure scope manipulation
                            .targetMethods(Matchers.annotatedWith(Valid.class)))
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Service {

        public void call(@NotNull Object arg) {

        }

        @Valid
        public Object call2(@NotNull Object arg) {

        }
    }

    static class Service2 {

        public void call(@NotNull Object arg) {

        }

        @Valid
        public Object call2(@NotNull Object arg) {

        }
    }
}
