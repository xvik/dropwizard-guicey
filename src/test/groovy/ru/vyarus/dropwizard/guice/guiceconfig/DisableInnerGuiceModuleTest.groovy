package ru.vyarus.dropwizard.guice.guiceconfig

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Key
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingletonInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 04.09.2019
 */
@TestGuiceyApp(App)
class DisableInnerGuiceModuleTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector

    def "Check one inner guice module excluded"() {

        when: "application started"
        then: "excluded module's extension not found"
        info.getExtensions(EagerSingletonInstaller) as Set == [Ext1, Ext3] as Set

        then: "binding in disabled module also removed"
        injector.getExistingBinding(Key.get(FooService)) == null

        then: "binding in other module is not removed"
        injector.getExistingBinding(Key.get(BarService)) != null
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new Module())
                    .disableModules(InnerModule1)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    static class Module extends AbstractModule {
        @Override
        protected void configure() {
            install(new InnerModule1())
            install(new InnerModule2())
            bind(Ext1).asEagerSingleton()
        }
    }

    static class InnerModule1 extends AbstractModule {
        @Override
        protected void configure() {
            install(new SubInnerModule())
            bind(Ext2).asEagerSingleton()
            bind(FooService)
        }
    }

    static class SubInnerModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(SubExt2).asEagerSingleton()
        }
    }

    static class InnerModule2 extends AbstractModule {
        @Override
        protected void configure() {
            bind(Ext3).asEagerSingleton()
            bind(BarService)
        }
    }

    @EagerSingleton
    static class Ext1 {}

    @EagerSingleton
    static class Ext2 {}

    @EagerSingleton
    static class Ext3 {}

    @EagerSingleton
    static class SubExt2 {}

    static class FooService {}

    static class BarService {}
}
