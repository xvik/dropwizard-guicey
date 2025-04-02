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
            assert SharedConfigurationState.lookup(app, GlobalBundle).isPresent()
            assert !SharedConfigurationState.lookup(app, ChildBundle).isPresent()
            assert SharedConfigurationState.lookup(app, EqualBundle).isPresent()
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class GlobalBundle implements GuiceyBundle {
        static boolean called
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.shareState(GlobalBundle, "12")
            bootstrap.whenSharedStateReady(GlobalBundle, { assert it == "12"; called = true})
            assert bootstrap.sharedState(GlobalBundle, null) != null
        }
    }

    static class ChildBundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            assert bootstrap.sharedStateOrFail(GlobalBundle, "no state") == "12"

            try {
                // access other state
                bootstrap.sharedStateOrFail(ChildBundle, "ups")
                assert false
            } catch (IllegalStateException ex) {
                assert ex.message == 'ups'
            }
        }

        @Override
        void run(GuiceyEnvironment environment) throws Exception {
            assert environment.sharedState(GlobalBundle).get() == "12"
            assert environment.sharedStateOrFail(GlobalBundle, "sds") == "12"

            try {
                // access other state
                environment.sharedStateOrFail(ChildBundle, "ups")
                assert false
            } catch (IllegalStateException ex) {
                assert ex.message == 'ups'
            }

            // check state sharing in run phase
            environment.shareState(Map, "foo")
            assert environment.sharedState(Map, null) == "foo"
            assert environment.sharedState(List, { "baa" }) == "baa"
        }
    }

    static class EqualBundle implements GuiceyBundle {

        static boolean called

        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            def state = bootstrap.sharedState(EqualBundle, { "13" })
            assert state == "13"

            assert bootstrap.sharedState(EqualBundle).get() == "13"
        }

        @Override
        void run(GuiceyEnvironment environment) throws Exception {
            environment.whenSharedStateReady(EqualBundle, { assert it == "13"; called = true})
            assert environment.sharedState(EqualBundle).get() == "13"
            assert environment.sharedStateOrFail(EqualBundle, "sds") == "13"
        }
    }
}
