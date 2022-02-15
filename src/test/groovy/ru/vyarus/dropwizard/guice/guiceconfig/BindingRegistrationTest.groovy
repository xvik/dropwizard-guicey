package ru.vyarus.dropwizard.guice.guiceconfig

import com.google.inject.AbstractModule
import com.google.inject.Inject
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 04.09.2019
 */
@TestGuiceyApp(App)
class BindingRegistrationTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check extensions registration form bindings"() {

        expect: "direct extension recognized"
        ExtensionItemInfo ext = info.getInfo(Ext)
        ext.isGuiceBinding()
        ext.getRegistrationScope().getType() == Module
        ext.getRegistrationScopeType() == ConfigScope.Module
        ext.getRegistrationAttempts() == 1

        and: "deep extension recognized"
        ExtensionItemInfo dext = info.getInfo(DeepExt)
        dext.isGuiceBinding()
        dext.getRegistrationScope().getType() == Module
        ext.getRegistrationScopeType() == ConfigScope.Module
        dext.getRegistrationAttempts() == 1
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new Module())
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Module extends AbstractModule {
        @Override
        protected void configure() {
            install(new DeepModule())
            bind(Ext).asEagerSingleton()
        }
    }

    static class DeepModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(DeepExt).asEagerSingleton()
        }
    }

    @EagerSingleton
    static class Ext {}

    @EagerSingleton
    static class DeepExt {}
}
