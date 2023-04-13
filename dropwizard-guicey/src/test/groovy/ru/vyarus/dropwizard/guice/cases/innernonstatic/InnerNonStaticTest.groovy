package ru.vyarus.dropwizard.guice.cases.innernonstatic

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.lifecycle.Managed
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 29.10.2018
 */
@TestGuiceyApp(App)
class InnerNonStaticTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info;

    def "Check inner classes scan"() {

        expect:
        info.getExtensions(ManagedInstaller).empty

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .enableAutoConfig(App.class.package.name)
                    .build());
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            // this will create implicit inner class (non static!) which must not be detected
            environment.lifecycle().manage(new Managed() {
                @Override
                void start() throws Exception {

                }

                @Override
                void stop() throws Exception {

                }
            })
        }
    }
}
