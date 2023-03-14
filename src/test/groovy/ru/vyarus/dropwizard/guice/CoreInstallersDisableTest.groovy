package ru.vyarus.dropwizard.guice

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 13.08.2016
 */
@TestGuiceyApp(EmptyApp)
class CoreInstallersDisableTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check no installers registered"() {

        expect: "no installers"
        info.getInstallers().empty
    }

    static class EmptyApp extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .disableBundleLookup()
                    .noDefaultInstallers()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}