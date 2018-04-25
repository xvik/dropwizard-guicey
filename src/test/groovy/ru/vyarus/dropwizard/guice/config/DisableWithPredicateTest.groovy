package ru.vyarus.dropwizard.guice.config

import com.google.inject.AbstractModule
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.context.Filters
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject
import javax.ws.rs.Path

import static ru.vyarus.dropwizard.guice.module.context.Disables.type

/**
 * @author Vyacheslav Rusakov
 * @since 09.04.2018
 */
@UseGuiceyApp(App)
class DisableWithPredicateTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check configuration items disabling with matcher"() {

        expect: "items disabled"
        info.getBundles().contains(SampleBundle2)  // to not specify bundles from test lookup
        !info.getBundles().contains(SampleBundle)
        info.getBundlesDisabled() == [SampleBundle]

        info.getModules().contains(SampleModule2)
        !info.getModules().contains(SampleModule1)
        info.getModulesDisabled() == [SampleModule1]

        info.getExtensions().containsAll(SampleExtension2, InstalledExtension)
        !info.getExtensions().contains(SampleExtension1)
        !info.getExtensions().contains(NeverInstalledExtension)
        info.getExtensionsDisabled() == [SampleExtension1]

        info.getInstallersDisabled() == [ManagedInstaller]

        and: "correct disable scope"
        info.data.getItems(Filters.disabledBy(ConfigScope.Application.type)) as Set ==
                [SampleBundle, SampleModule1] as Set
        info.data.getItems(Filters.disabledBy(ConfigScope.Configurator.type)) as Set ==
                [SampleExtension1, ManagedInstaller] as Set
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
            // listener will call disable AFTER items registration (post processing on predicate registration)
                    .listen(new Listener())
            // predicate registered before items registration and will disable items by event
                    .disable(type(SampleBundle, SampleModule1))

                    .bundles(new SampleBundle(), new SampleBundle2())
                    .modules(new SampleModule1(), new SampleModule2())
                    .extensions(SampleExtension1, SampleExtension2)

                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Listener extends GuiceyLifecycleAdapter implements GuiceyConfigurator {
        @Override
        void configure(GuiceBundle.Builder builder) {
            builder.disable(type(SampleExtension1, ManagedInstaller))
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

    @Path('/1')
    static class SampleExtension1 {}

    @Path('/2')
    static class SampleExtension2 {}

    @Path('/never')
    static class NeverInstalledExtension {}

    @Path('/installed')
    static class InstalledExtension {}
}
