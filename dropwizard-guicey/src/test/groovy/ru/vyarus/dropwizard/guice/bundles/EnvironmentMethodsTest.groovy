package ru.vyarus.dropwizard.guice.bundles

import com.google.inject.AbstractModule
import com.google.inject.TypeLiteral
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import jakarta.inject.Provider
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import jakarta.inject.Inject
import jakarta.ws.rs.core.FeatureContext

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

        Bundle.service.get() instanceof Service
        Bundle.genericService.get() instanceof GenericService
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
        public static Provider<Service> service;
        public static Provider<GenericService<String>> genericService;

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

            service = environment.getProvider(Service)
            genericService = environment.getProvider(new TypeLiteral<GenericService<String>>() {})
        }
    }

    static class DisabledModule extends AbstractModule {}

    static class Module extends AbstractModule {
        @Override
        protected void configure() {
            bind(Service)
            bind(new TypeLiteral<GenericService<String>>() {})
        }
    }

    static class OverrideModule extends AbstractModule {}

    @EagerSingleton
    static class Ext {}

    static class Feature implements jakarta.ws.rs.core.Feature {
        static int called

        @Override
        boolean configure(FeatureContext context) {
            called++
            return false
        }
    }

    static class Feature2 implements jakarta.ws.rs.core.Feature {
        static int called

        @Override
        boolean configure(FeatureContext context) {
            called++
            return false
        }
    }

    static class Service {}
    static class GenericService<T> {}
}
