package ru.vyarus.dropwizard.guice.guiceconfig.prvt


import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.PrivateModule
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import io.dropwizard.lifecycle.Managed
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.GuiceyOptions
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 02.02.2025
 */
@TestGuiceyApp(App)
class PrivateModulesAnalysisDisabledTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector

    def "Check extensions registration form private binding"() {

        expect: "extension not detected"
        info.getModules().contains(Module)
        !info.getInfo(Ext)
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new Module())
                    .option(GuiceyOptions.AnalyzePrivateGuiceModules, false)
                    .printAllGuiceBindings()
                    .build()
            )
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Module extends PrivateModule {
        @Override
        protected void configure() {
            bind(Ext)
            expose(Ext)
        }
    }

    static class Ext implements Managed {
    }
}
