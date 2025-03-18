package ru.vyarus.dropwizard.guice.config.sharedstate

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 28.09.2019
 */
@TestGuiceyApp(App)
class SharedStateUsageTest extends Specification {

    def "Check shared state usage"() {

        expect: "all asserts ok"
        GlobalBundle.called
        EqualBundle.called
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new GlobalBundle(), new ChildBundle())
                    .bundles(new EqualBundle(), new EqualBundle())
                    .build())

            def app = bootstrap.getApplication()
            assert SharedConfigurationState.get(app) != null
            assert SharedConfigurationState.lookup(app, GlobalState).isPresent()
            assert !SharedConfigurationState.lookup(app, ChildState).isPresent()
            assert SharedConfigurationState.lookup(app, EqualState).isPresent()
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class GlobalBundle implements GuiceyBundle {
        static boolean called
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.shareState(GlobalState, new GlobalState(value: "12"))
            bootstrap.whenSharedStateReady(GlobalState, { assert it.value == "12"; called = true})
            assert bootstrap.sharedState(GlobalState, null) != null
        }
    }

    static class ChildBundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            assert bootstrap.sharedStateOrFail(GlobalState, "no state").value == "12"

            try {
                // access other state
                bootstrap.sharedStateOrFail(ChildState, "ups")
                assert false
            } catch (IllegalStateException ex) {
                assert ex.message == 'ups'
            }
        }

        @Override
        void run(GuiceyEnvironment environment) throws Exception {
            assert environment.sharedState(GlobalState).get().value == "12"
            assert environment.sharedStateOrFail(GlobalState, "sds").value == "12"

            try {
                // access other state
                environment.sharedStateOrFail(ChildState, "ups")
                assert false
            } catch (IllegalStateException ex) {
                assert ex.message == 'ups'
            }

            // check state sharing in run phase
            environment.shareState(String, "foo")
            assert environment.sharedState(String, null) == "foo"
            assert environment.sharedState(List, { ["baa"] })[0] == "baa"
        }
    }

    static class EqualBundle implements GuiceyBundle {

        static boolean called

        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            def state = bootstrap.sharedState(EqualState, { new EqualState(value: "13") })
            assert state.value == "13"

            assert bootstrap.sharedState(EqualState).get().value == "13"
        }

        @Override
        void run(GuiceyEnvironment environment) throws Exception {
            environment.whenSharedStateReady(EqualState, { assert it.value == "13"; called = true})
            assert environment.sharedState(EqualState).get().value == "13"
            assert environment.sharedStateOrFail(EqualState, "sds").value == "13"
        }
    }

    static class GlobalState {
        String value
    }

    static class ChildState {
        String value
    }

    static class EqualState {
        String value
    }
}
