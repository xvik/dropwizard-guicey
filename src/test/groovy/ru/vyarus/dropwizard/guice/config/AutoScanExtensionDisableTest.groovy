package ru.vyarus.dropwizard.guice.config

import com.google.inject.Inject
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.support.feature.DummyTask
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 07.04.2018
 */
@UseGuiceyApp(App)
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
