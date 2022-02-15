package ru.vyarus.dropwizard.guice.test.bind

import com.google.inject.AbstractModule
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.binding.BindingsOverrideInjectorFactory
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 24.06.2018
 */
@TestGuiceyApp(App)
class OverridingBindingsOverrideTest extends Specification {

    @Inject
    Service service

    def "Check overrides override"() {

        expect:
        service.action() == "override overridden"
    }

    def "Check too late registration"() {

        when: "register module after app start"
        BindingsOverrideInjectorFactory.override(new ModuleExtExt())
        then:
        def ex = thrown(IllegalStateException)
        ex.message == "Too late overriding bindings registration: injector was already created"
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
            // original binding
                    .modules(new Module())
            // overriding binding
                    .modulesOverride(new ModuleExt())
                    .printLifecyclePhasesDetailed()
                    .injectorFactory(new BindingsOverrideInjectorFactory())
                    .build())

            // override overridden definition
            BindingsOverrideInjectorFactory.override(new ModuleExtExt())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Service {
        String action() {
            return "origin"
        }
    }

    static class ServiceExt extends Service {
        String action() {
            return "override"
        }
    }

    static class ServiceExtExt extends Service {
        String action() {
            return "override overridden"
        }
    }

    static class Module extends AbstractModule {
        @Override
        protected void configure() {
            bind(Service)
        }
    }

    static class ModuleExt extends AbstractModule {
        @Override
        protected void configure() {
            bind(Service).to(ServiceExt)
        }
    }

    static class ModuleExtExt extends AbstractModule {
        @Override
        protected void configure() {
            bind(Service).to(ServiceExtExt)
        }
    }
}
