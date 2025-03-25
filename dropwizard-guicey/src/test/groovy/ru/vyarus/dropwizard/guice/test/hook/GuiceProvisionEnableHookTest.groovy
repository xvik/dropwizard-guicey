package ru.vyarus.dropwizard.guice.test.hook

import com.google.inject.Inject
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.debug.hook.GuiceProvisionTimeHook
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 25.03.2025
 */
@TestGuiceyApp(App)
class GuiceProvisionEnableHookTest extends Specification {

    void cleanup() {
        ConfigurationHooksSupport.reset()
    }

    @Inject
    GuiceyConfigurationInfo info

    def "Guice provision hook enable"() {

        expect:
        info.getData().getHooks().contains(GuiceProvisionTimeHook)
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            System.setProperty(ConfigurationHooksSupport.HOOKS_PROPERTY, GuiceProvisionTimeHook.ALIAS)
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
