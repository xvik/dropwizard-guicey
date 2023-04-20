package ru.vyarus.dropwizard.guice.bundles

import com.google.inject.AbstractModule
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import javax.inject.Inject
import javax.ws.rs.core.FeatureContext

/**
 * @author Vyacheslav Rusakov
 * @since 10.09.2019
 */
@TestDropwizardApp(App)
class EnvironmentMethodsTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check configuration correctness"() {

        expect:
        info.getGuiceyBundles().contains(Bundle)
        info.getExtensionsDisabled() == [Ext]
        info.getNormalModules().contains(Module)
        info.getOverridingModules() == [OverrideModule]
        info.getModulesDisabled() == [DisabledModule]
        Feature.called == 1
        Feature2.called == 1
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
            bootstrap.extensions(Ext)
        }

        @Override
        void run(GuiceyEnvironment environment) {
            environment
                    .modules(new DisabledModule(), new Module())
                    .modulesOverride(new OverrideModule())
                    .disableModules(DisabledModule)
                    .disableExtensions(Ext)
                    .register(Feature)
                    .register(new Feature2())
        }
    }

    static class DisabledModule extends AbstractModule {}

    static class Module extends AbstractModule {}

    static class OverrideModule extends AbstractModule {}

    @EagerSingleton
    static class Ext {}

    static class Feature implements javax.ws.rs.core.Feature {
        static int called

        @Override
        boolean configure(FeatureContext context) {
            called++
            return false
        }
    }

    static class Feature2 implements javax.ws.rs.core.Feature {
        static int called

        @Override
        boolean configure(FeatureContext context) {
            called++
            return false
        }
    }
}
