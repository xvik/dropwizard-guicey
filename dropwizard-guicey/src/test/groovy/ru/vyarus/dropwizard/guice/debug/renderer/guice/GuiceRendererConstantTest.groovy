package ru.vyarus.dropwizard.guice.debug.renderer.guice

import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.name.Names
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.ConstantModule
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceBindingsRenderer
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 20.08.2019
 */
@TestGuiceyApp(App)
class GuiceRendererConstantTest extends Specification {

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
    └── ConstantModule               (r.v.d.g.d.r.g.support)
        ├── <typeconverter>                       ConstantModule\$2                                at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.ConstantModule.configure(ConstantModule.java:17)
        ├── instance             [@Singleton]     @Named("smth") Integer                          at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.ConstantModule.configure(ConstantModule.java:28)
        └── instance             [@Singleton]     @Named("string") String                         at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.ConstantModule.configure(ConstantModule.java:29)


    2 UNDECLARED bindings
    ├── @Named("string") Integer (converted by com.google.inject.internal.TypeConverterBindingProcessor\$5) (java.lang)
    └── @Named("string") Sample (converted by ru.vyarus.dropwizard.guice.debug.renderer.guice.support.ConstantModule\$2) (r.v.d.g.d.r.g.s.ConstantModule)


    BINDING CHAINS
    ├── @Named("string") Integer  --[converted]-->  @Named("string") String
    └── @Named("string") Sample  --[converted]-->  @Named("string") String
""" as String;
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new ConstantModule())
                    .printGuiceBindings()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            def injector = InjectorLookup.getInjector(this).get()
            // force conversion binding
            injector.getInstance(Key.get(Integer.class, Names.named("string")))
            // conversion with custom converter
            injector.getInstance(Key.get(ConstantModule.Sample.class, Names.named("string")))
        }
    }


    String render(GuiceConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }
}
