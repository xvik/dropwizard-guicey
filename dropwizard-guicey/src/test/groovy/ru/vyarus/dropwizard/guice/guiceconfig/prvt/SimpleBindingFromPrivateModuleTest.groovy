package ru.vyarus.dropwizard.guice.guiceconfig.prvt

import com.google.inject.Exposed
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.PrivateModule
import com.google.inject.Provides
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import io.dropwizard.lifecycle.Managed
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 29.01.2025
 */
@TestGuiceyApp(App)
class SimpleBindingFromPrivateModuleTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector

    def "Check extensions registration form private binding"() {

        expect: "module detected"
        info.getModules().contains(Module)
        injector.getInstance(Ext)
        injector.getInstance(Ext2)

        and: "extension recognized"
        ExtensionItemInfo ext = info.getInfo(Ext)
        ext != null
        ext.isGuiceBinding()
        ext.getRegistrationScope().getType() == Module
        ext.getRegistrationScopeType() == ConfigScope.Module
        ext.getRegistrationAttempts() == 1

        and: "extension from provider recognized"
        ExtensionItemInfo ext2 = info.getInfo(Ext2)
        ext2 != null
        ext2.isGuiceBinding()
        ext2.getRegistrationScope().getType() == Module
        ext2.getRegistrationScopeType() == ConfigScope.Module
        ext2.getRegistrationAttempts() == 1

        and: "inner extension not recognized"
        info.getInfo(Bind) == null
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new Module())
                    .printAllGuiceBindings()
                    .build()
            )
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Module extends PrivateModule {
        @Override
        protected void configure() {
            // visible extension
            bind(Ext)
            expose(Ext)

            // not visible extension
            bind(Bind).to(InnerExt)
        }

        @Provides @Exposed
        Ext2 provide() {
            new Ext2()
        }
    }

    static interface Bind {}

    static class Ext implements Managed {
    }

    static class Ext2 implements Managed {
    }

    static class InnerExt implements Bind, Managed {}
}
