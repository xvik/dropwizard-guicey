package ru.vyarus.dropwizard.guice.config.unique

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueModule
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 11.09.2019
 */
@UseGuiceyApp(App)
class MultiSourceIgnoresTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Chcek correct ignores counting"() {

        setup:
        List<ItemId> bundles = info.getData().getItems(Bundle)

        when: "check extension"
        ItemInfo item = info.getInfo(Ext)

        then: "correct ignores"
        item.getIgnoresByScope(Bundle) == 3
        item.getIgnoresByScope(bundles[0]) == 1
        item.getIgnoresByScope(bundles[1]) == 2

        when: "check modules"
        item = info.getInfo(Module)

        then: "correct ignores"
        item.getIgnoresByScope(Bundle) == 3
        item.getIgnoresByScope(bundles[0]) == 1
        item.getIgnoresByScope(bundles[1]) == 2

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new Bundle(), new Bundle())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Bundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.extensions(Ext, Ext)
            bootstrap.modules(new Module(), new Module())
        }
    }

    @EagerSingleton
    static class Ext {}

    static class Module extends UniqueModule {}
}
