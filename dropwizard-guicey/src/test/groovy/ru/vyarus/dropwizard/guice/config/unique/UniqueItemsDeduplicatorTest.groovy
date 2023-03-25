package ru.vyarus.dropwizard.guice.config.unique

import com.google.inject.Inject
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 24.09.2019
 */
@TestGuiceyApp(App)
class UniqueItemsDeduplicatorTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check duplicates not allowed for selected types"() {


        expect: "Foo2 bundle registered twice"
        info.getInfos(Foo2Bundle).size() == 2

        and: "Foo registered just once"
        info.getInfo(FooBundle).registrationAttempts == 2
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
            // with legacy mode only 1 registration will occur
                    .uniqueItems(FooBundle)
                    .bundles(new FooBundle(), new Foo2Bundle(), new MiddleBundle())
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class FooBundle implements GuiceyBundle {}

    static class Foo2Bundle implements GuiceyBundle {}

    static class MiddleBundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            // FooBundle should be detected as duplicate due to its equals method
            bootstrap.bundles(new FooBundle(), new Foo2Bundle())
        }
    }
}
