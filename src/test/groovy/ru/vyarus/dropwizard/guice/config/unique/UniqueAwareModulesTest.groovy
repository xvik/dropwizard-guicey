package ru.vyarus.dropwizard.guice.config.unique


import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueDropwizardAwareModule
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 13.12.2019
 */
@UseGuiceyApp(App)
class UniqueAwareModulesTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check duplicates allowed and equals handling"() {

        expect: "Unique module registered once"
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
                    .modules(new UniqueMod())
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
            bootstrap.modules(new UniqueMod())
        }
    }

    static class UniqueMod extends UniqueDropwizardAwareModule {
        @Override
        protected void configure() {
            // check objects autowired
            assert configuration() != null
            assert bootstrap() != null
            assert environment() != null
        }
    }
}
