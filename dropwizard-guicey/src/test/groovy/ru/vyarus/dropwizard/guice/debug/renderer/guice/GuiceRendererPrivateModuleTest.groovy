package ru.vyarus.dropwizard.guice.debug.renderer.guice

import com.google.inject.Injector
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.OuterModule
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceBindingsRenderer
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2019
 */
@TestGuiceyApp(App)
class GuiceRendererPrivateModuleTest extends Specification {

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

    4 MODULES with 6 bindings
    │
    └── OuterModule                  (r.v.d.g.d.r.g.s.privt)
        │
        └── InnerModule                  (r.v.d.g.d.r.g.s.privt)    *PRIVATE
            ├── untargetted          [@Prototype]     InnerService                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.InnerModule.configure(InnerModule.java:16)
            ├── untargetted          [@Prototype]     OuterService                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.InnerModule.configure(InnerModule.java:17) *EXPOSED
            ├── linkedkey            [@Prototype]     OService --> IndirectOuterService               at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.InnerModule.configure(InnerModule.java:19) *EXPOSED
            ├── exposed              [@Prototype]     OuterService                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.InnerModule.configure(InnerModule.java:21)
            ├── exposed              [@Prototype]     OService                                        at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.InnerModule.configure(InnerModule.java:22)
            ├── providerinstance     [@Prototype]     OuterProviderService                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.InnerModule.getService(InnerModule.java:27) *EXPOSED
            ├── exposed              [@Prototype]     OuterProviderService                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.InnerModule.getService(InnerModule.java:27)
            │
            └── Inner2Module                 (r.v.d.g.d.r.g.s.privt)
                ├── untargetted          [@Prototype]     InnerService2                                   at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.Inner2Module.configure(Inner2Module.java:14)
                │
                └── Inner3Module                 (r.v.d.g.d.r.g.s.privt)    *PRIVATE
                    └── untargetted          [@Prototype]     OutServ                                         at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.privt.Inner3Module.configure(Inner3Module.java:13) *EXPOSED


    BINDING CHAINS
    └── OService  --[linked]-->  IndirectOuterService
""" as String;
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new OuterModule())
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
