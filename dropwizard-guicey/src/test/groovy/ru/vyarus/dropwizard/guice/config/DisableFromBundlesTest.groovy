package ru.vyarus.dropwizard.guice.config

import com.google.inject.AbstractModule
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 26.04.2018
 */
@TestGuiceyApp(App)
class DisableFromBundlesTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check disables"() {

        expect: "all disables applied"
        info.getModulesDisabled() == [XMod]
        info.getExtensionsDisabled() == [XExtension]
        info.getInstallersDisabled() == [ManagedInstaller]
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

    static class Bundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.disableInstallers(ManagedInstaller)
                    .disableExtensions(XExtension)
                    .disableModules(XMod)

        }
    }

    static class XExtension {}

    static class XMod extends AbstractModule {}

}
