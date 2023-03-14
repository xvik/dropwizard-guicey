package ru.vyarus.dropwizard.guice.config.unique

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 11.07.2019
 */
@TestGuiceyApp(App)
class DuplicateInstanceInitTest extends AbstractTest {

    def "Check duplicate bundles not initialized"() {

        expect: "duplicate bundle ignored"
        Bundle.initCount == 2
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
            // FooBundle has correct equals
                    .bundles(new Bundle(1), new Bundle(2), new Bundle(1))
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Bundle implements GuiceyBundle {

        static initCount = 0

        int value

        Bundle(int value) {
            this.value = value
        }

        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            initCount++;
        }

        @Override
        boolean equals(Object obj) {
            return obj instanceof Bundle && value.equals(obj.value)
        }
    }
}
