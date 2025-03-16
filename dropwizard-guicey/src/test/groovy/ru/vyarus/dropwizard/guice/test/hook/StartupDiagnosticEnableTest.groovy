package ru.vyarus.dropwizard.guice.test.hook

import com.google.inject.Inject
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.debug.hook.StartupTimeHook
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 14.03.2025
 */
@TestGuiceyApp(App)
class StartupDiagnosticEnableTest extends Specification {

    void cleanup() {
        ConfigurationHooksSupport.reset()
    }

    @Inject
    GuiceyConfigurationInfo info

    def "Startup hook enable"() {

        expect:
        info.getData().getHooks().contains(StartupTimeHook)
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            System.setProperty(ConfigurationHooksSupport.HOOKS_PROPERTY, StartupTimeHook.ALIAS)
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
