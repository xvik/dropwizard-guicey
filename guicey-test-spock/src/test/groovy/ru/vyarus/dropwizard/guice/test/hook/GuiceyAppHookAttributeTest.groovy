package ru.vyarus.dropwizard.guice.test.hook

import com.google.inject.Binder
import com.google.inject.Module
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 13.04.2018
 */
@UseGuiceyApp(value = App, hooks = Hook)
class GuiceyAppHookAttributeTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check hook attribute works"() {

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

    static class Hook implements GuiceyConfigurationHook {
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
