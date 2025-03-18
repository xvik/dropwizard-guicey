package ru.vyarus.dropwizard.guice.config.sharedstate

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import jakarta.inject.Inject
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
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
@TestGuiceyApp(value = App, hooks = XHook)
class SharedHookStateTest extends Specification {

    @Inject
    Bootstrap bootstrap

    def "Check hook access to shared memory"() {

        expect:
        SharedConfigurationState.lookup(bootstrap.getApplication(), XHookState).get().value == "12"
        SharedConfigurationState.lookup(bootstrap.getApplication(), BundleState).get().value == "15"
        SharedConfigurationState.lookup(bootstrap.getApplication(), SharedHookState).get().value == "20"
        Bundle.called
        Bundle.called2
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new Bundle())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class XHook implements GuiceyConfigurationHook {
        @Override
        void configure(GuiceBundle.Builder builder) {
            builder.withSharedState({
                it.put(XHookState, new XHookState(value: "12"))
            })
        }
    }

    static class Bundle implements GuiceyBundle {

        static boolean called
        static boolean called2

        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.shareState(SharedHookState, new SharedHookState(value: "20"))
            bootstrap.whenSharedStateReady(BundleState, { assert it.value == "15"; called = true })
            assert bootstrap.sharedStateOrFail(XHookState, "problem").value == "12"
        }

        @Override
        void run(GuiceyEnvironment environment) throws Exception {
            environment.whenSharedStateReady(BundleState, { assert it.value == "15"; called2 = true})
            environment.shareState(BundleState, new BundleState(value: "15"))
        }
    }

    static class XHookState {
        String value
    }

    static class BundleState {
        String value
    }

    static class SharedHookState {
        String value
    }
}
