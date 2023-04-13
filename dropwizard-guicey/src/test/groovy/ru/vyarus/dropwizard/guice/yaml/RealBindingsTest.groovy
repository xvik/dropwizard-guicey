package ru.vyarus.dropwizard.guice.yaml

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.yaml.bind.Config
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 25.06.2018
 */
@TestGuiceyApp(App)
class RealBindingsTest extends Specification {

    @Inject
    Configuration direct
    @Inject
    @Config
    Configuration directQualified
    @Inject
    @Config
    AppConfig config
    @Inject
    @Config
    Iface configIface
    @Inject
    @Config
    AppConfig.SubConfig subConfig
    @Inject
    @Config("sub.sub")
    String value

    def "Check binding correctness"() {

        expect: "all bindings correct"
        direct != null
        directQualified == direct
        direct == config
        config == configIface
        subConfig != null
        subConfig.sub == value
        value == 'sample'
    }

    static class App extends Application<AppConfig> {

        @Override
        void initialize(Bootstrap<AppConfig> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .printCustomConfigurationBindings()
                    .build())
        }

        @Override
        void run(AppConfig configuration, Environment environment) throws Exception {
        }
    }

    static class AppConfig extends Configuration implements Iface {

        SubConfig sub = new SubConfig()

        static class SubConfig {
            String sub = 'sample';
        }
    }

    interface Iface {

    }
}
