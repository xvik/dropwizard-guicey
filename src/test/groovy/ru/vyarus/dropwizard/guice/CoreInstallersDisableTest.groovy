package ru.vyarus.dropwizard.guice

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject


/**
 * @author Vyacheslav Rusakov
 * @since 13.08.2016
 */
@UseGuiceyApp(EmptyApp)
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