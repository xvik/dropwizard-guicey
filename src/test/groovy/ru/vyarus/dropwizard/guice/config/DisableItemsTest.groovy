package ru.vyarus.dropwizard.guice.config

import com.google.inject.AbstractModule
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.ConfiguredBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.Filters
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.inject.Inject
import javax.ws.rs.Path

import static ru.vyarus.dropwizard.guice.module.context.info.ItemId.typesOnly

/**
 * @author Vyacheslav Rusakov
 * @since 07.04.2018
 */
@TestGuiceyApp(App)
class DisableItemsTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check configuration items disabling"() {

        expect: "items disabled"
        !info.getDropwizardBundles().contains(DBundle)
        info.getGuiceyBundles().contains(SampleBundle2)  // to not specify bundles from test lookup
        !info.getGuiceyBundles().contains(SampleBundle)
        info.getBundlesDisabled() as Set == [DBundle, SampleBundle] as Set

        info.getModules().contains(SampleModule2)
        !info.getModules().contains(SampleModule1)
        info.getModulesDisabled() == [SampleModule1]

        info.getExtensions().containsAll(SampleExtension2, InstalledExtension)
        !info.getExtensions().contains(SampleExtension1)
        !info.getExtensions().contains(NeverInstalledExtension)
        info.getExtensionsDisabled() == [SampleExtension1]

        info.getInstallersDisabled() == [ManagedInstaller]

        and: "correct disable scope"
        typesOnly(info.data.getItems(Filters.disabledBy(Application))) as Set ==
                [SampleBundle, SampleModule1, SampleExtension1, ManagedInstaller, DBundle] as Set
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .dropwizardBundles(new DBundle())
                    .bundles(new SampleBundle(), new SampleBundle2())
                    .modules(new SampleModule1(), new SampleModule2())
                    .extensions(SampleExtension1, SampleExtension2)

                    .disableDropwizardBundles(DBundle)
                    .disableBundles(SampleBundle)
                    .disableModules(SampleModule1)
                    .disableExtensions(SampleExtension1)
                    .disableInstallers(ManagedInstaller)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class SampleBundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            // extension will never be installed because bundle is disabled and so not processed
            bootstrap.extensions(NeverInstalledExtension)
        }
    }

    static class SampleBundle2 implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.extensions(InstalledExtension)
        }
    }

    static class SampleModule1 extends AbstractModule {}

    static class SampleModule2 extends AbstractModule {}

    static class DBundle implements ConfiguredBundle {}

    @Path('/1')
    static class SampleExtension1 {}

    @Path('/2')
    static class SampleExtension2 {}

    @Path('/never')
    static class NeverInstalledExtension {}

    @Path('/installed')
    static class InstalledExtension {}
}
