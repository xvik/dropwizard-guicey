package ru.vyarus.dropwizard.guice.config.unique

import com.google.inject.AbstractModule
import com.google.inject.Inject
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueModule
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 04.07.2019
 */
@TestGuiceyApp(App)
class MultipleModuleInstancesTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check duplicates allowed and equals handling"() {

        expect: "Mod module registered two times"
        List<ModuleItemInfo> mods = info.getInfos(Mod)
        with(mods[0]) {
            registrationScope == ItemId.from(Application)
            registeredBy == [ItemId.from(Application)] as Set
            registrationAttempts == 1

            getInstance() instanceof Mod
            getInstanceCount() == 1

            getIgnoresByScope(Application) == 0
            getIgnoresByScope(MiddleBundle) == 0
        }

        with(mods[1]) {
            registrationScope == ItemId.from(MiddleBundle)
            registeredBy == [ItemId.from(MiddleBundle)] as Set
            registrationAttempts == 1

            getInstance() instanceof Mod
            getInstance() != mods[0].getInstance()
            getInstanceCount() == 2

            getIgnoresByScope(Application) == 0
            getIgnoresByScope(MiddleBundle) == 0
        }

        and: "Foo registered just once"
        ModuleItemInfo umod = info.getInfo(UniqueMod)
        with(umod) {
            registrationScope == ItemId.from(Application)
            registeredBy == [ItemId.from(Application), ItemId.from(MiddleBundle)] as Set
            registrationAttempts == 2

            getInstance() instanceof UniqueMod
            getInstanceCount() == 1

            getIgnoresByScope(Application) == 0
            getIgnoresByScope(MiddleBundle) == 1
        }
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new MiddleBundle())
                    .modules(new Mod(), new UniqueMod())
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
            bootstrap.modules(new Mod(), new UniqueMod())
        }
    }

    static class Mod extends AbstractModule {}

    static class UniqueMod extends UniqueModule {}
}
