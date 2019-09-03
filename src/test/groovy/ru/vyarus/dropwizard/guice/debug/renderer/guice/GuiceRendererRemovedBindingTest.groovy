package ru.vyarus.dropwizard.guice.debug.renderer.guice

import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.DisableExtensionModule
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceBindingsRenderer
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 03.09.2019
 */
@UseGuiceyApp(App)
class GuiceRendererRemovedBindingTest extends Specification {

    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    @Inject
    Injector injector
    GuiceBindingsRenderer renderer

    void setup() {
        renderer = new GuiceBindingsRenderer(injector)
    }

    def "Check removed bindings render"() {

        expect:
        render(new GuiceConfig()
                .hideGuiceBindings()
                .hideGuiceyBindings()) == """

    1 MODULES with 5 bindings
    │
    └── DisableExtensionModule       (r.v.d.g.d.r.g.support)
        ├── untargetted          [@Prototype]     Res1                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.DisableExtensionModule.configure(DisableExtensionModule.java:16) *EXTENSION, REMOVED
        ├── linkedkey            [@Prototype]     Res2                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.DisableExtensionModule.configure(DisableExtensionModule.java:17) *EXTENSION, REMOVED
        ├── instance             [@Singleton]     Res3                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.DisableExtensionModule.configure(DisableExtensionModule.java:18) *EXTENSION, REMOVED
        ├── providerkey          [@Prototype]     Res4                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.DisableExtensionModule.configure(DisableExtensionModule.java:19) *EXTENSION, REMOVED
        └── providerinstance     [@Prototype]     Res5                                            at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.DisableExtensionModule.configure(DisableExtensionModule.java:20) *EXTENSION, REMOVED
""" as String;
    }


    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new DisableExtensionModule())
                    .disableExtensions(DisableExtensionModule.Res1,
                            DisableExtensionModule.Res2,
                            DisableExtensionModule.Res3,
                            DisableExtensionModule.Res4,
                            DisableExtensionModule.Res5)
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
