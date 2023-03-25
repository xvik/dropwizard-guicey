package ru.vyarus.dropwizard.guice.config

import com.google.inject.Inject
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.support.feature.DummyTask
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 07.04.2018
 */
@TestGuiceyApp(App)
class AutoScanExtensionDisableTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check auto scan extension disable"() {

        expect: "extension disabled"
        info.getExtensionsDisabled() == [DummyTask]
        !info.getExtensions().contains(DummyTask)
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .enableAutoConfig("ru.vyarus.dropwizard.guice.support.feature")
                    .disableExtensions(DummyTask)
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
