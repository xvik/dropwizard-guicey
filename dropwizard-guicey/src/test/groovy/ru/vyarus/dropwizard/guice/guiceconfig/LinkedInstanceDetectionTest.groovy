package ru.vyarus.dropwizard.guice.guiceconfig

import com.google.inject.AbstractModule
import com.google.inject.Inject
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.ConfigItem
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import jakarta.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 13.12.2019
 */
@TestGuiceyApp(App)
class LinkedInstanceDetectionTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check bindings exclusion"() {

        expect: "extension not found"
        !info.getData().getItems(ConfigItem.Extension).contains(ItemId.from(Ext))
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
            // instance should be analyzed as extension because it's not guice managed object
            bind(Base).toInstance(new Ext())
        }
    }

    static interface Base {
    }

    @Path("/")
    static class Ext implements Base {
    }
}
