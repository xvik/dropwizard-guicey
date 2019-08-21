package ru.vyarus.dropwizard.guice.config.debug.renderer.guice

import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.ProviderMethodModule
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.GuiceBindingsRenderer
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2019
 */
@UseGuiceyApp(App)
class GuiceRendererProviderMethodTest extends Specification {

    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    @Inject
    Injector injector
    GuiceBindingsRenderer renderer

    void setup() {
        renderer = new GuiceBindingsRenderer(injector)
    }

    def "Check custom render"() {

        expect:
        render(new GuiceConfig()
                .hideGuiceBindings()
                .hideGuiceyBindings()) == """

    1 MODULES with 1 bindings
    │
    └── ProviderMethodModule         (r.v.d.g.c.d.r.g.support)
        └── providerinstance     [@Prototype]     Sample                                          at ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.ProviderMethodModule.provide(ProviderMethodModule.java:19)
""" as String;
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new ProviderMethodModule())
                    .printGuiceBindings()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    String render(GuiceConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }
}
