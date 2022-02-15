package ru.vyarus.dropwizard.guice.debug.renderer.guice

import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.GuiceyOptions
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.TransitiveModule
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceBindingsRenderer
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 19.09.2019
 */
@TestGuiceyApp(App)
class GuiceRendererDisabledModuleNoAnalysisTest extends Specification {

    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    @Inject
    Injector injector
    GuiceBindingsRenderer renderer

    void setup() {
        renderer = new GuiceBindingsRenderer(injector)
    }

    def "Check removed modules render"() {

        expect: "entire tree rendered as nothing removed"
        render(new GuiceConfig()
                .hideGuiceBindings()
                .hideGuiceyBindings()) == """

    3 MODULES with 4 bindings
    │
    └── TransitiveModule             (r.v.d.g.d.r.g.support)
        ├── untargetted          [@Prototype]     Res1                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.TransitiveModule.configure(TransitiveModule.java:15)
        ├── untargetted          [@Prototype]     Res2                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.TransitiveModule.configure(TransitiveModule.java:16)
        │
        └── Inner                        (r.v.d.g.d.r.g.s.TransitiveModule)
            ├── untargetted          [@Prototype]     Res3                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.TransitiveModule\$Inner.configure(TransitiveModule.java:23)
            │
            └── SubInner                     (r.v.d.g.d.r.g.s.TransitiveModule)
                └── untargetted          [@Prototype]     Res4                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.TransitiveModule\$SubInner.configure(TransitiveModule.java:31)
""" as String;
    }


    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new TransitiveModule())
                    .disableExtensions(TransitiveModule.Res2)
                    .disableModules(TransitiveModule.Inner)
                    .printGuiceBindings()
                    .option(GuiceyOptions.AnalyzeGuiceModules, false)
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
