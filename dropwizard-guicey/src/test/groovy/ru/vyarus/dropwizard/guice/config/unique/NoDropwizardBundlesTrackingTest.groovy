package ru.vyarus.dropwizard.guice.config.unique

import com.google.inject.Inject
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.ConfiguredBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.GuiceyOptions
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.DropwizardBundleItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 28.07.2019
 */
@TestGuiceyApp(App)
class NoDropwizardBundlesTrackingTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check duplicates allowed and equals handling"() {


        expect: "bundle registered 4 times (2 times directly without tracking)"
        DBundle.executed == 4
        List<DropwizardBundleItemInfo> bundles = info.getInfos(DBundle)
        bundles.size() == 2
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
            registeredBy == [ItemId.from(Application)] as Set
            registrationAttempts == 1

            getInstance() instanceof DBundle
            getInstanceCount() == 2

            getIgnoresByScope(Application) == 0
            getIgnoresByScope(MiddleBundle) == 0
        }
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .dropwizardBundles(new DBundle(1), new DBundle(2), new MiddleBundle())
                    .option(GuiceyOptions.TrackDropwizardBundles, false)
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
