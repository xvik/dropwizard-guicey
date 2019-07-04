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
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 04.07.2019
 */
@UseGuiceyApp(App)
class MultipleModuleInstancesTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check duplicates allowed and equals handling"() {

        expect: "Mod module registered two times"
        ModuleItemInfo mod = info.data.getInfo(Mod)
        with(mod) {
            registrationScopes == [Application, MiddleBundle]
            registrations == 2

            getRegistrationsByScope(Application).size() == 1
            getDuplicatesByScope(Application).size() == 0

            getRegistrationsByScope(MiddleBundle).size() == 1
            getDuplicatesByScope(MiddleBundle).size() == 0
        }

        and: "Foo registered just once"
        ModuleItemInfo umod = info.data.getInfo(UniqueMod)
        with(umod) {
            registrationScopes == [Application]
            registrations == 1

            getRegistrationsByScope(Application).size() == 1
            getDuplicatesByScope(Application).size() == 0

            getRegistrationsByScope(MiddleBundle).size() == 0
            getDuplicatesByScope(MiddleBundle).size() == 1
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

    static class UniqueMod extends AbstractModule {
        @Override
        boolean equals(Object obj) {
            return obj.getClass().equals(getClass())
        }
    }
}
