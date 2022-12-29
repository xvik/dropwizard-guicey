package ru.vyarus.dropwizard.guice.lifecycle

import com.google.inject.Binder
import com.google.inject.Module
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyFeatureInstaller
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
import ru.vyarus.dropwizard.guice.support.feature.DummyPlugin1
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 21.04.2018
 */
@TestDropwizardApp(App)
class LifecycleEventsTest extends AbstractTest {

    def "Check lifecycle events"() {

        expect: "app started normally, only visual verification"
        true
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .enableAutoConfig("ru.vyarus.dropwizard.guice.support.feature")
                    .searchCommands()
                    .modules(new Mod())
                    .bundles(new HK2DebugBundle())
                    .disableModules(Mod)
                    .disableInstallers(JerseyFeatureInstaller)
                    .disableBundles(HK2DebugBundle)
                    .disableExtensions(DummyPlugin1)
                    .printLifecyclePhases()
                    .printDiagnosticInfo()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Mod implements Module {
        @Override
        void configure(Binder binder) {

        }
    }

}