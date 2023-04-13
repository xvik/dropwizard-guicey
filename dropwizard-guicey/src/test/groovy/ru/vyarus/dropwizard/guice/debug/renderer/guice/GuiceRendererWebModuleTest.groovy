package ru.vyarus.dropwizard.guice.debug.renderer.guice

import com.google.inject.Injector
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.WebModule
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceBindingsRenderer
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 20.08.2019
 */
@TestDropwizardApp(App)
class GuiceRendererWebModuleTest extends Specification {
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

    1 MODULES with 2 bindings
    │
    └── WebModule                    (r.v.d.g.d.r.g.support)    *WEB
        ├── <filterkey>          [@Singleton]     WFilter (/1/*)                                  at com.google.inject.servlet.FiltersModuleBuilder\$FilterKeyBindingBuilderImpl.through(FiltersModuleBuilder.java:106)
        ├── <filterinstance>     [@Singleton]     WFilter (/2/*)                                  at com.google.inject.servlet.FiltersModuleBuilder\$FilterKeyBindingBuilderImpl.through(FiltersModuleBuilder.java:106)
        ├── <servletkey>         [@Singleton]     WServlet (/1/foo)                               at com.google.inject.servlet.ServletsModuleBuilder\$ServletKeyBindingBuilderImpl.with(ServletsModuleBuilder.java:116)
        └── <servletinstance>    [@Singleton]     WServlet (/2/foo)                               at com.google.inject.servlet.ServletsModuleBuilder\$ServletKeyBindingBuilderImpl.with(ServletsModuleBuilder.java:116)
""" as String;
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new WebModule())
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
