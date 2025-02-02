package ru.vyarus.dropwizard.guice.guiceconfig.prvt

import com.google.inject.AbstractModule
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
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 02.02.2025
 */
@TestGuiceyApp(App)
class ExposedBindingDeclaredInSubModuleTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector

    def "Check extensions registration form private binding"() {

        expect: "module detected"
        info.getModules().contains(Module)
        injector.getInstance(Ext)
        injector.getInstance(Ext2)
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
            install(new SubModule())
            install(new PrivateSubModule())
            expose(Ext)
            expose(Ext2)
        }
    }

    private static class SubModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Ext)
        }
    }

    private static class PrivateSubModule extends PrivateModule {
        @Override
        protected void configure() {
            bind(Ext2)
            expose(Ext2)
        }
    }

    static class Ext implements Managed {
    }

    static class Ext2 implements Managed {
    }
}
