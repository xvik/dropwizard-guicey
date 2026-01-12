package ru.vyarus.dropwizard.guice.debug.renderer.guice

import com.google.inject.AbstractModule
import com.google.inject.Injector
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import jakarta.inject.Inject
import jakarta.inject.Singleton
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceBindingsRenderer
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.module.support.scope.Prototype
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 14.11.2024
 */
@TestDropwizardApp(App)
class GuiceRendererSingletonDetectionTest extends Specification {

    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    @Inject
    Injector injector
    GuiceBindingsRenderer renderer

    void setup() {
        renderer = new GuiceBindingsRenderer(injector)
    }

    def "Check singleton detection"() {

        expect:
        render(new GuiceConfig()
                .hideGuiceBindings()
                .hideGuiceyBindings())
                .replace('IndyInterface.java:344', 'IndyInterface.java:321')== """

    1 MODULES with 12 bindings
    │
    └── Module                       (r.v.d.g.d.r.g.GuiceRendererSingletonDetectionTest)
        ├── linkedkey            [@Prototype]     Bind4 --> Ext4                                  at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)
        ├── linkedkey            [@Prototype]     Bind5 --> Ext5                                  at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)
        ├── linkedkey            [@Prototype]     LongBind --> LongExt1                           at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)
        ├── linkedkey            [@Singleton]     Bind1 --> Ext1                                  at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)
        ├── linkedkey            [@Singleton]     Bind2 --> Ext2                                  at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)
        ├── linkedkey            [@Singleton]     Bind3 --> Ext3                                  at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)
        ├── linkedkey            [@Singleton]     LongExt1 --> LongExt2                           at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)
        ├── untargetted          [@Prototype]     Simple4                                         at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)
        ├── untargetted          [@Prototype]     Simple5                                         at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)
        ├── untargetted          [@Singleton]     Simple1                                         at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)
        ├── untargetted          [@Singleton]     Simple2                                         at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)
        └── untargetted          [@Singleton]     Simple3                                         at org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(IndyInterface.java:321)


    BINDING CHAINS
    ├── Bind1  --[linked]-->  Ext1
    ├── Bind2  --[linked]-->  Ext2
    ├── Bind3  --[linked]-->  Ext3
    ├── Bind4  --[linked]-->  Ext4
    ├── Bind5  --[linked]-->  Ext5
    └── LongBind  --[linked]-->  LongExt1  --[linked]-->  LongExt2
""" as String
    }


    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new Module())
                    .printGuiceBindings()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Module extends AbstractModule {
        @Override
        protected void configure() {
            bind(Simple1)
            bind(Simple2).in(Singleton.class)
            bind(Simple3).asEagerSingleton()
            bind(Simple4)
            bind(Simple5)

            // linked
            bind(Bind1).to(Ext1)
            bind(Bind2).to(Ext2).in(Singleton.class)
            bind(Bind3).to(Ext3).asEagerSingleton()
            bind(Bind4).to(Ext4)
            bind(Bind5).to(Ext5)

            bind(LongBind).to(LongExt1)
            bind(LongExt1).to(LongExt2)
        }
    }

    @Singleton
    static class Simple1 {}
    static class Simple2 {}
    static class Simple3 {}
    static class Simple4 {}
    @Prototype
    static class Simple5 {}

    static interface Bind1 {}
    @Singleton
    static class Ext1 implements Bind1 {}

    static interface Bind2 {}
    static class Ext2 implements Bind2 {}

    static interface Bind3 {}
    static class Ext3 implements Bind3 {}

    static interface Bind4 {}
    static class Ext4 implements Bind4 {}

    static interface Bind5 {}
    @Prototype
    static class Ext5 implements Bind5 {}


    static interface LongBind {}
    static class LongExt1 implements LongBind {}
    @Singleton
    static class LongExt2 extends LongExt1 {}

    String render(GuiceConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }
}
