package ru.vyarus.dropwizard.guice.config.unique

import com.google.inject.Inject
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.Foo2Bundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.GuiceyBundleItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 04.07.2019
 */
@UseGuiceyApp(App)
class MultipleBundleInstancesTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check duplicates allowed and equals handling"() {


        expect: "Foo2 bundle registered two times"
        List<GuiceyBundleItemInfo> foo2s = info.getInfos(Foo2Bundle)
        with(foo2s[0]) {
            registrationScope == ItemId.from(Application)
            registeredBy == [ItemId.from(Application)] as Set
            registrationAttempts == 1

            getInstance() instanceof Foo2Bundle
            getInstanceCount() == 1

            getIgnoresByScope(Application) == 0
            getIgnoresByScope(MiddleBundle) == 0
        }
        with(foo2s[1]) {
            registrationScope == ItemId.from(MiddleBundle)
            registeredBy == [ItemId.from(MiddleBundle)] as Set
            registrationAttempts == 1

            getInstance() instanceof Foo2Bundle
            getInstance() != foo2s[0].getInstance()
            getInstanceCount() == 2
        }

        and: "Foo registered just once"
        GuiceyBundleItemInfo foo = info.getInfo(FooBundle)
        with(foo) {
            registrationScope == ItemId.from(Application)
            registeredBy == [ItemId.from(Application), ItemId.from(MiddleBundle)] as Set
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
            // FooBundle has correct equals
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
