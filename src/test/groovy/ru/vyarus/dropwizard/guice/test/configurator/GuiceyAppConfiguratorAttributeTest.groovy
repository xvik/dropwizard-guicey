package ru.vyarus.dropwizard.guice.test.configurator

import com.google.inject.Binder
import com.google.inject.Module
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 13.04.2018
 */
@UseGuiceyApp(value = App, configurators = Conf)
class GuiceyAppConfiguratorAttributeTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check configurator attribute works"() {

        expect: "module registered"
        info.getModules().contains(XMod)
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Conf implements GuiceyConfigurator {
        @Override
        void configure(GuiceBundle.Builder builder) {
            builder.modules(new XMod())
        }
    }

    static class XMod implements Module {
        @Override
        void configure(Binder binder) {

        }
    }
}
