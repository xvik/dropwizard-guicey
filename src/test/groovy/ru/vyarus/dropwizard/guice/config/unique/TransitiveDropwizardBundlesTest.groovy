package ru.vyarus.dropwizard.guice.config.unique

import com.google.inject.Inject
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.ConfiguredBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.DropwizardBundleItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 28.07.2019
 */
@UseGuiceyApp(App)
class TransitiveDropwizardBundlesTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check duplicates allowed and equals handling"() {


        expect: "bundle registered 3 times"
        DBundle.executed == 3
        List<DropwizardBundleItemInfo> bundles = info.getInfos(DBundle)
        bundles.size() == 3
        with(bundles[0]) {
            registrationScope == ItemId.from(Application)
            registeredBy == [ItemId.from(Application)] as Set
            registrationAttempts == 1

            getInstance() instanceof DBundle
            getInstanceCount() == 1

            getIgnoresByScope(Application) == 0
            getIgnoresByScope(MiddleBundle) == 0
        }
        with(bundles[1]) {
            registrationScope == ItemId.from(Application)
            registeredBy == [ItemId.from(Application), ItemId.from(MiddleBundle)] as Set
            registrationAttempts == 2

            getInstance() instanceof DBundle
            getInstanceCount() == 2

            getIgnoresByScope(Application) == 0
            getIgnoresByScope(MiddleBundle) == 1
        }
        with(bundles[2]) {
            registrationScope == ItemId.from(MiddleBundle)
            registeredBy == [ItemId.from(MiddleBundle)] as Set
            registrationAttempts == 1

            getInstance() instanceof DBundle
            getInstanceCount() == 3

            getIgnoresByScope(Application) == 0
            getIgnoresByScope(MiddleBundle) == 0
        }
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .dropwizardBundles(new DBundle(1), new DBundle(2), new MiddleBundle())
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    static class DBundle implements ConfiguredBundle {

        static int executed

        int num

        DBundle(int num) {
            this.num = num
        }

        @Override
        void initialize(Bootstrap bootstrap) {
            executed++
        }

        boolean equals(o) {
            return o instanceof DBundle && num == o.num
        }
    }

    static class MiddleBundle implements ConfiguredBundle {
        @Override
        void initialize(Bootstrap bootstrap) {
            bootstrap.addBundle(new DBundle(2))
            bootstrap.addBundle(new DBundle(3))
        }
    }
}
