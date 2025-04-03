package ru.vyarus.dropwizard.guice.config.protect

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import javax.inject.Inject
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.GuiceyOptions
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 28.01.2025
 */
@TestGuiceyApp(App)
class ProtectedClassesScanTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check protected classes scan"() {

        expect: "only public detected"
        info.getExtensions().contains(PublicExt1)
        info.getExtensions().contains(ProtectedExt1)
        info.getExtensions().contains(PublicExt1.ProtectedExt2)
        !info.getExtensions().contains(ProtectedExt3)
        !info.getExtensions().contains(PublicExt1.ProtectedExt4)
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .enableAutoConfig()
                    .option(GuiceyOptions.ScanProtectedClasses, true)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}
