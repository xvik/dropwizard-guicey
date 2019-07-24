package ru.vyarus.dropwizard.guice.config

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
 * @since 24.07.2019
 */
@UseGuiceyApp(App)
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
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .dropwizardBundles(new DBundle())
                    .disableDropwizardBundles(DBundle)
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
}
