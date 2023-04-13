package ru.vyarus.dropwizard.guice.debug.renderer.web

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.web.support.GuiceWebModule
import ru.vyarus.dropwizard.guice.debug.report.web.MappingsConfig
import ru.vyarus.dropwizard.guice.debug.report.web.WebMappingsRenderer
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 23.10.2019
 */
@TestDropwizardApp(App)
class GuiceFilterRenderTest extends Specification {
    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    @Inject
    Environment environment
    @Inject
    GuiceyConfigurationInfo info
    WebMappingsRenderer renderer

    void setup() {
        renderer = new WebMappingsRenderer(environment, info)
    }

    def "Check guice context render"() {

        expect:
        render(new MappingsConfig()
                .showMainContext()
                .showGuiceMappings()) == """

    MAIN /
    │
    └── filter     /*                   async   GuiceFilter                  (c.g.inject.servlet)                                    [REQUEST]       Guice Filter
        ├── guicefilter     /1/*                         GFilter                        r.v.d.g.d.r.w.support.GuiceWebModule
        ├── guicefilter     /1/abc?/.*           regex   GRegexFilter                   r.v.d.g.d.r.w.support.GuiceWebModule
        ├── guicefilter     /1/foo                       instance of GFilterInstance    r.v.d.g.d.r.w.support.GuiceWebModule
        ├── guiceservlet    /2/*                         GServlet                       r.v.d.g.d.r.w.support.GuiceWebModule
        ├── guiceservlet    /2/abc?/             regex   GRegexServlet                  r.v.d.g.d.r.w.support.GuiceWebModule
        └── guiceservlet    /2/foo                       instance of GServletInstance   r.v.d.g.d.r.w.support.GuiceWebModule
""" as String;
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new GuiceWebModule())
                    .printWebMappings()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    String render(MappingsConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }
}
