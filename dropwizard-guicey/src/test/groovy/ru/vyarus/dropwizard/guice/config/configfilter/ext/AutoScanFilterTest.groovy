package ru.vyarus.dropwizard.guice.config.configfilter.ext

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import io.dropwizard.lifecycle.Managed
import javax.inject.Inject
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.GuiceyOptions
import ru.vyarus.dropwizard.guice.config.configfilter.Component
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 06.02.2025
 */
@TestGuiceyApp(App)
class AutoScanFilterTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check filters applied"() {

        expect:
        info.getExtensions().contains(Ext1)
        !info.getExtensions().contains(Ext2)
        // guice bindings would also appear in filter!
        App.filtered.containsAll([Ext1, Ext2])
    }

    static class App extends Application<Configuration> {
        static List<Class<?>> filtered = []

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .enableAutoConfig()
                    .option(GuiceyOptions.ScanProtectedClasses, true)
                    .autoConfigFilter {
                        filtered.add(it)
                        return it.isAnnotationPresent(Component)
                    }
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Component
    static class Ext1 implements Managed {}

    static class Ext2 implements Managed {}
}

