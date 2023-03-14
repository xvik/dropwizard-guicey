package ru.vyarus.dropwizard.guice.cases.multicases

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.lifecycle.Managed
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 01.08.2016
 */
@TestGuiceyApp(App)
class MultiFeatureTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check multifeature installation"() {

        expect: "feature installed by only 1 installer"
        def info = info.<ExtensionItemInfo> getInfo(MultiExtension)
        info.getRegisteredBy() == [ItemId.from(Application)] as Set
        info.installedBy == ManagedInstaller

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(MultiExtension)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    @EagerSingleton
    static class MultiExtension implements Managed {

        @Override
        void start() throws Exception {

        }

        @Override
        void stop() throws Exception {

        }
    }
}