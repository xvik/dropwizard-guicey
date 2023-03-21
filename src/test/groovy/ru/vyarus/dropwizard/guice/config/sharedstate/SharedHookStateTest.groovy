package ru.vyarus.dropwizard.guice.config.sharedstate

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 28.09.2019
 */
@TestGuiceyApp(value = App, hooks = XHook)
class SharedHookStateTest extends Specification {

    @Inject
    Bootstrap bootstrap

    def "Check hook access to shared memeory"() {

        expect:
        SharedConfigurationState.lookup(bootstrap.getApplication(), XHook).get() == "12"
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
                it.put(XHook, "12")
            })
        }
    }

    static class Bundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            assert bootstrap.sharedStateOrFail(XHook, "ugr") == "12"
        }
    }
}
