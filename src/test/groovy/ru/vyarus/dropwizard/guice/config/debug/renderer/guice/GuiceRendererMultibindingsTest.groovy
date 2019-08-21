package ru.vyarus.dropwizard.guice.config.debug.renderer.guice

import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.MultibindingsModule
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.GuiceBindingsRenderer
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 20.08.2019
 */
@UseGuiceyApp(App)
class GuiceRendererMultibindingsTest extends Specification {

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

    2 MODULES with 9 bindings
    │
    └── MultibindingsModule          (r.v.d.g.c.d.r.g.support)
        ├── linkedkey            [@Prototype]     @Element Plugin (multibinding)                  at ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.MultibindingsModule.configure(MultibindingsModule.java:17)
        ├── instance             [@Singleton]     @Element Plugin (multibinding)                  at ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.MultibindingsModule.configure(MultibindingsModule.java:18)
        ├── providerinstance     [@Prototype]     @Element Map.Entry<String, Provider<KeyedPlugin>> (multibinding)   at ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.MultibindingsModule.configure(MultibindingsModule.java:22)
        ├── linkedkey            [@Prototype]     @Element KeyedPlugin (multibinding)             at ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.MultibindingsModule.configure(MultibindingsModule.java:22)
        ├── instance             [@Singleton]     @Element KeyedPlugin (multibinding)             at ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.MultibindingsModule.configure(MultibindingsModule.java:23)
        ├── providerinstance     [@Prototype]     @Element Map.Entry<String, Provider<KeyedPlugin>> (multibinding)   at ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.MultibindingsModule.configure(MultibindingsModule.java:23)
        ├── providerinstance     [@Prototype]     OptService                                      at ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.MultibindingsModule.configure(MultibindingsModule.java:25)
        ├── linkedkey            [@Prototype]     @Default OptService (optional binding)          at ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.MultibindingsModule.configure(MultibindingsModule.java:25)
        │
        └── OverideModule                (r.v.d.g.c.d.r.g.s.MultibindingsModule)
            ├── providerinstance     [@Prototype]     OptService                                      at ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.MultibindingsModule\$OverideModule.configure(MultibindingsModule.java:48)
            └── linkedkey            [@Prototype]     @Actual OptService (optional binding)           at ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support.MultibindingsModule\$OverideModule.configure(MultibindingsModule.java:48)


    BINDING CHAINS
    ├── @Actual OptService  --[linked]-->  ActualImpl
    ├── @Default OptService  --[linked]-->  DefImpl
    ├── @Element KeyedPlugin  --[linked]-->  MyKeyedPlugin
    └── @Element Plugin  --[linked]-->  MyPlugin
""" as String;
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new MultibindingsModule())
                    .printGuiceBindings()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            println InjectorLookup.getInjector(this).get().getInstance(MultibindingsModule.OptService.class)
        }
    }


    String render(GuiceConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }
}
