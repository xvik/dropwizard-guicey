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
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 13.12.2019
 */
@TestGuiceyApp(App)
class LinkedKeyDetectionTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check bindings exclusion"() {

        expect: "extension found"
        info.<ExtensionItemInfo> getInfo(Ext).isGuiceBinding()
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new LinkedExtensionModule())
                    .printGuiceBindings()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    static class LinkedExtensionModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Base).to(Ext)
        }
    }

    static interface Base {
    }

    @Path("/")
    static class Ext implements Base {
    }
}
