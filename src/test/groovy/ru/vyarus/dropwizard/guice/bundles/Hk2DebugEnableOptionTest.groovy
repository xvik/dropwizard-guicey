package ru.vyarus.dropwizard.guice.bundles

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.VoidBundleLookup
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 27.06.2016
 */
@UseGuiceyApp(SampleApp)
class Hk2DebugEnableOptionTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check hk2 debug bundle enable option"() {

        expect: "bundle enabled"
        info.guiceyBundles == [HK2DebugBundle]
    }

    static class SampleApp extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundleLookup(new VoidBundleLookup())
                    .noDefaultInstallers()
                    .strictScopeControl()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
