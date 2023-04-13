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
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import jakarta.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 13.12.2019
 */
@TestGuiceyApp(App)
class LinkedKeyRemoveTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check bindings exclusion"() {

        expect: "context started: linked binding was also removed"
        info.<ExtensionItemInfo> getInfo(ExtImpl).isGuiceBinding()
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new LinkedExtensionModule())
                    .disableExtensions(ExtImpl)
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
            bind(Ext).to(ExtImpl)
        }
    }

    static interface Base {}


    static interface Ext extends Base {}

    @Path("/")
    static class ExtImpl implements Ext {}
}
