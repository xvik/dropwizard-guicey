package ru.vyarus.dropwizard.guice.guiceconfig.prvt

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Key
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
 * @since 14.11.2024
 */
@TestGuiceyApp(App)
class DisablePrivateExtensionTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector

    def "Check private binding remove for disabled extension"() {

        expect: "extension ignored recognized"
        info.getInfo(Ext) != null
        info.getInfo(Ext2) != null

        and: "binding removed"
        injector.getExistingBinding(Key.get(Bind)) == null
        injector.getExistingBinding(Key.get(Ext2)) != null
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new Module())
                    .disableExtensions(Ext)
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

            // ext must remain
            bind(Ext2)
            expose(Ext2)
        }
    }

    static interface Bind {}

    static class Ext implements Bind, Managed {
    }

    static class Ext2 implements Managed {
    }
}