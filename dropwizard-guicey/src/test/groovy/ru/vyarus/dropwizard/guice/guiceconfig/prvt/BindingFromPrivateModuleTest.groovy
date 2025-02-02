package ru.vyarus.dropwizard.guice.guiceconfig.prvt


import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.PrivateModule
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
 * @since 18.10.2024
 */
@TestGuiceyApp(App)
class BindingFromPrivateModuleTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector

    def "Check extensions registration form private binding"() {

        expect: "module detected"
        info.getModules().contains(Module)
        injector.getInstance(Ext)

        and: "extension recognized"
        ExtensionItemInfo ext = info.getInfo(Ext)
        ext != null
        ext.isGuiceBinding()
        ext.getRegistrationScope().getType() == Module
        ext.getRegistrationScopeType() == ConfigScope.Module
        ext.getRegistrationAttempts() == 1

        and: "inner extension not recognized"
        info.getInfo(InnerExt) == null
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
            bind(Bind).to(Ext)
            expose(Bind)

            // not visible extension
            bind(InnerExt)
        }
    }

    static interface Bind {}

    static class Ext implements Bind, Managed {
    }

    static class InnerExt implements Managed {}
}
