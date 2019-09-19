package ru.vyarus.dropwizard.guice.debug.renderer.guice

import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.TransitiveModule
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceBindingsRenderer
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 19.09.2019
 */
@UseGuiceyApp(App)
class GuiceRendererRemovedModuleTest extends Specification {

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

        expect: "not removed bindings shown"
        render(new GuiceConfig()
                .hideGuiceBindings()
                .hideGuiceyBindings()) == """

    2 MODULES with 2 bindings
    │
    └── TransitiveModule             (r.v.d.g.d.r.g.support)
        ├── untargetted          [@Prototype]     Res1                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.TransitiveModule.configure(TransitiveModule.java:15) *EXTENSION
        ├── untargetted          [@Prototype]     Res2                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.TransitiveModule.configure(TransitiveModule.java:16) *EXTENSION, REMOVED
        └── Inner                        (r.v.d.g.d.r.g.s.TransitiveModule) *REMOVED
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
