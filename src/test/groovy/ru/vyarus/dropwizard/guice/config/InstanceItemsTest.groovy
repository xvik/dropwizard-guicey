package ru.vyarus.dropwizard.guice.config

import com.google.inject.AbstractModule
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 10.09.2019
 */
@TestGuiceyApp(App)
class InstanceItemsTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check info correctness"() {

        expect: "correct number of items provided"
        info.getData().getInfos(Module).size() == 3
        info.getData().getItems(Module).size() == 3

        info.getData().getInfos(Ext).size() == 1
        info.getData().getItems(Ext).size() == 1
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(Ext, Ext, Ext)
                    .modules(new Module(), new Module(), new Module())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    @EagerSingleton
    static class Ext {}

    static class Module extends AbstractModule {}
}

