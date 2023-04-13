package ru.vyarus.dropwizard.guice.config

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.context.ConfigItem
import ru.vyarus.dropwizard.guice.module.context.ConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 28.09.2019
 */
@TestGuiceyApp(App)
class ConfigurationInfoEdgeCasesTest extends Specification {

    @Inject
    ConfigurationInfo info

    def "Check items selection"() {

        expect:
        !info.getItems(ConfigItem.Bundle).isEmpty()
        info.getItems(ConfigItem.DropwizardBundle).isEmpty()

        and: "get not registered items info"
        info.getItems(Bundle3).isEmpty()

        and: "get info for not registered instance"
        info.getInfo(ItemId.from(Bundle3)) == null

        and: "get instance type by id"
        info.getInfo(ItemId.from(App.bundle2)) != null

        and: "get class type by id"
        info.getInfo(ItemId.from(Ext)) != null

        and: "get infos by class"
        info.getInfos(Bundle1).size() == 2
        info.getInfos(Bundle2).size() == 1
        info.getInfos(Bundle3).size() == 0
        info.getInfos(Ext).size() == 1
    }

    def "Check instance access by class"() {

        when: "accessing instance type by class"
        def id = ItemId.from(Bundle1)
        info.getInfo(id)
        then: "err"
        def ex = thrown(IllegalStateException)
        ex.message == "Class id descriptor ($id) can't be used to reach instance configurations: 2. Use getInfos(class) instead."

    }

    static class App extends Application<Configuration> {
        static Bundle2 bundle2 = new Bundle2()

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new Bundle1(), new Bundle1(), bundle2)
                    .extensions(Ext)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Bundle1 implements GuiceyBundle {}

    static class Bundle2 implements GuiceyBundle {}

    static class Bundle3 implements GuiceyBundle {}

    @EagerSingleton
    static class Ext {}
}
