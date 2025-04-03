package ru.vyarus.dropwizard.guice.config.disable

import com.google.inject.Inject
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.ConfiguredBundle
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.DropwizardBundleItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 24.07.2019
 */
@TestGuiceyApp(App)
class DisableDropwizardBundleTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check duplicates allowed and equals handling"() {


        expect: "bundle disabled"
        DBundle.executed == 0
        List<DropwizardBundleItemInfo> bundles = info.getInfos(DBundle)
        bundles.size() == 1
        with(bundles[0]) {
            disabledBy == [ItemId.from(Application)] as Set
        }

        and: "never registered bundle disabled"
        List<DropwizardBundleItemInfo> disabled = info.getInfos(DBundle2)
        bundles.size() == 1
        !disabled[0].isRegistered()
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .dropwizardBundles(new DBundle())
                    .disableDropwizardBundles(DBundle, DBundle2)
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    static class DBundle implements ConfiguredBundle {

        static int executed

        @Override
        void initialize(Bootstrap bootstrap) {
            executed++
        }
    }

    static class DBundle2 implements ConfiguredBundle {}
}
