package ru.vyarus.dropwizard.guice.config.unique

import com.google.inject.Inject
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.Foo2Bundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.GuiceyBundleItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.context.unique.LegacyModeDuplicatesDetector
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 04.07.2019
 */
@TestGuiceyApp(App)
class LegacyDuplicatesPolicyTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check duplicates not allowed"() {


        expect: "Foo2 bundle registered just once"
        GuiceyBundleItemInfo foo2 = info.getInfo(Foo2Bundle)
        with(foo2) {
            registrationScope == ItemId.from(Application)
            registrationAttempts == 2

            getInstance() instanceof Foo2Bundle
            getInstanceCount() == 1

            getIgnoresByScope(Application) == 0
            getIgnoresByScope(MiddleBundle) == 1
        }

        and: "Foo registered just once"
        GuiceyBundleItemInfo foo = info.getInfo(FooBundle)
        with(foo) {
            registrationScope == ItemId.from(Application)
            registrationAttempts == 2

            getInstance() instanceof FooBundle
            getInstanceCount() == 1

            getIgnoresByScope(Application) == 0
            getIgnoresByScope(MiddleBundle) == 1
        }
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
            // with legacy mode only 1 registration will occur
                    .duplicateConfigDetector(new LegacyModeDuplicatesDetector())
                    .bundles(new FooBundle(), new Foo2Bundle(), new MiddleBundle())
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class MiddleBundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            // FooBundle should be detected as duplicate due to its equals method
            bootstrap.bundles(new FooBundle(), new Foo2Bundle())
        }
    }
}
