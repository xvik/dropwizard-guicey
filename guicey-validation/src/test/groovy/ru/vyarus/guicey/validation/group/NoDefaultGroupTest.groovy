package ru.vyarus.guicey.validation.group

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guice.validator.group.ValidationContext
import ru.vyarus.guicey.validation.ValidationBundle
import spock.lang.Specification

import javax.inject.Inject
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.groups.Default

/**
 * @author Vyacheslav Rusakov
 * @since 07.09.2021
 */
@TestGuiceyApp(App)
class NoDefaultGroupTest extends Specification {

    @Inject
    Service service
    @Inject
    ValidationContext context

    def "Check default groups not used"() {

        when: "valid model used"
        service.call(new Model(foo: "sample", bar: null))
        then: "ok"
        true

        when: "call with default group"
        context.doWithGroups({
            service.call(new Model(foo: "sample", bar: null))
        }, Default)
        then: "error"
        thrown(ConstraintViolationException)
    }

    static class Model {

        @NotNull(groups = Group1)
        String foo
        @NotNull
        String bar
    }

    static class Service {

        @Group1
        void call(@Valid Model model) {
        }
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new ValidationBundle().strictGroupsDeclaration())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
