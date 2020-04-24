package ru.vyarus.dropwizard.guice.config.unique

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.unique.LegacyModeDuplicatesDetector
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 24.09.2019
 */
@UseGuiceyApp(App)
class DeduplicatorOverrideTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check deduplicator override"() {

        expect: "two bundle registrations - legacy deduplicator overridden"
        info.getInfos(Bundle).size() == 2
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .duplicateConfigDetector(new LegacyModeDuplicatesDetector())
                    // override detector implementation
                    .uniqueItems(App)
                    .bundles(new Bundle(), new Bundle())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    static class Bundle implements GuiceyBundle {}
}
