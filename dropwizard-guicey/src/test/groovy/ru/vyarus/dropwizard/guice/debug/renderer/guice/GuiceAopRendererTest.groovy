package ru.vyarus.dropwizard.guice.debug.renderer.guice

import com.google.inject.Injector
import com.google.inject.matcher.Matchers
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceAopConfig
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceAopMapRenderer
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 23.08.2019
 */
@TestDropwizardApp(App)
class GuiceAopRendererTest extends Specification {
    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    @Inject
    Injector injector
    GuiceAopMapRenderer renderer

    void setup() {
        renderer = new GuiceAopMapRenderer(injector)
    }

    def "Check default render"() {

        expect:
        render(new GuiceAopConfig()) == """

    2 AOP handlers declared
    ├── AopModule/Interceptor1                                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule.configure(AopModule.java:23)
    └── AopModule/Interceptor2                                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule.configure(AopModule.java:24)


    3 bindings affected by AOP
    │
    ├── GuiceyConfigurationInfo    (r.v.d.guice.module)
    │   ├── getActiveScopes()                                                 Interceptor1
    │   ├── getActiveScopes(boolean)                                          Interceptor1
    │   ├── getBundlesDisabled()                                              Interceptor1
    │   ├── getBundlesFromLookup()                                            Interceptor1
    │   ├── getCommands()                                                     Interceptor1
    │   ├── getConfigurationTree()                                            Interceptor1
    │   ├── getData()                                                         Interceptor1
    │   ├── getDirectBundles()                                                Interceptor1
    │   ├── getDropwizardBundleIds()                                          Interceptor1
    │   ├── getDropwizardBundles()                                            Interceptor1
    │   ├── getExtensions()                                                   Interceptor1
    │   ├── getExtensions(Class<? extends FeatureInstaller>)                  Interceptor1
    │   ├── getExtensionsDisabled()                                           Interceptor1
    │   ├── getExtensionsFromBindings()                                       Interceptor1
    │   ├── getExtensionsFromScan()                                           Interceptor1
    │   ├── getExtensionsOrdered(Class<? extends FeatureInstaller>)           Interceptor1
    │   ├── getExtensionsRegisteredManauallyOnly()                            Interceptor1
    │   ├── getExtensionsRegisteredManually()                                 Interceptor1
    │   ├── getGuiceyBundleIds()                                              Interceptor1
    │   ├── getGuiceyBundles()                                                Interceptor1
    │   ├── getGuiceyBundlesInInitOrder()                                     Interceptor1
    │   ├── getInfo(Class<? extends Object>)                                  Interceptor1
    │   ├── getInfos(Class<? extends Object>)                                 Interceptor1
    │   ├── getInstallers()                                                   Interceptor1
    │   ├── getInstallersDisabled()                                           Interceptor1
    │   ├── getInstallersFromScan()                                           Interceptor1
    │   ├── getInstallersOrdered()                                            Interceptor1
    │   ├── getItemsByScope(Class<? extends Object>)                          Interceptor1
    │   ├── getItemsByScope(ConfigScope)                                      Interceptor1
    │   ├── getItemsByScope(ItemId)                                           Interceptor1
    │   ├── getModuleIds()                                                    Interceptor1
    │   ├── getModules()                                                      Interceptor1
    │   ├── getModulesDisabled()                                              Interceptor1
    │   ├── getNormalModuleIds()                                              Interceptor1
    │   ├── getNormalModules()                                                Interceptor1
    │   ├── getOptions()                                                      Interceptor1
    │   ├── getOverridingModuleIds()                                          Interceptor1
    │   ├── getOverridingModules()                                            Interceptor1
    │   ├── getRelativelyInstalledBundles(Class<? extends Object>)            Interceptor1
    │   └── getStats()                                                        Interceptor1
    │
    ├── Service    (r.v.d.g.d.r.g.s.AopModule)
    │   ├── something()                                                       Interceptor1
    │   └── somethingElse(List)                                               Interceptor1
    │
    └── Service2    (r.v.d.g.d.r.g.s.AopModule)
        ├── something()                                                       Interceptor1
        └── somethingElse(List)                                               Interceptor1, Interceptor2
""" as String;
    }

    def "Check filter by type render"() {

        expect:
        render(new GuiceAopConfig()
                .types(Matchers.subclassesOf(AopModule.Service.class))) == """

    2 AOP handlers declared
    ├── AopModule/Interceptor1                                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule.configure(AopModule.java:23)
    └── AopModule/Interceptor2                                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule.configure(AopModule.java:24)


    1 bindings affected by AOP
    │
    └── Service    (r.v.d.g.d.r.g.s.AopModule)
        ├── something()                                                       Interceptor1
        └── somethingElse(List)                                               Interceptor1
""" as String;
    }

    def "Check filter by method render"() {

        expect:
        render(new GuiceAopConfig()
                .methods(Matchers.returns(Matchers.subclassesOf(List)))) == """

    2 AOP handlers declared
    ├── AopModule/Interceptor1                                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule.configure(AopModule.java:23)
    └── AopModule/Interceptor2                                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule.configure(AopModule.java:24)


    3 bindings affected by AOP
    │
    ├── GuiceyConfigurationInfo    (r.v.d.guice.module)
    │   ├── getBundlesDisabled()                                              Interceptor1
    │   ├── getBundlesFromLookup()                                            Interceptor1
    │   ├── getCommands()                                                     Interceptor1
    │   ├── getDirectBundles()                                                Interceptor1
    │   ├── getDropwizardBundleIds()                                          Interceptor1
    │   ├── getDropwizardBundles()                                            Interceptor1
    │   ├── getExtensions()                                                   Interceptor1
    │   ├── getExtensions(Class<? extends FeatureInstaller>)                  Interceptor1
    │   ├── getExtensionsDisabled()                                           Interceptor1
    │   ├── getExtensionsFromBindings()                                       Interceptor1
    │   ├── getExtensionsFromScan()                                           Interceptor1
    │   ├── getExtensionsOrdered(Class<? extends FeatureInstaller>)           Interceptor1
    │   ├── getExtensionsRegisteredManauallyOnly()                            Interceptor1
    │   ├── getExtensionsRegisteredManually()                                 Interceptor1
    │   ├── getGuiceyBundleIds()                                              Interceptor1
    │   ├── getGuiceyBundles()                                                Interceptor1
    │   ├── getGuiceyBundlesInInitOrder()                                     Interceptor1
    │   ├── getInfos(Class<? extends Object>)                                 Interceptor1
    │   ├── getInstallers()                                                   Interceptor1
    │   ├── getInstallersDisabled()                                           Interceptor1
    │   ├── getInstallersFromScan()                                           Interceptor1
    │   ├── getInstallersOrdered()                                            Interceptor1
    │   ├── getItemsByScope(Class<? extends Object>)                          Interceptor1
    │   ├── getItemsByScope(ConfigScope)                                      Interceptor1
    │   ├── getItemsByScope(ItemId)                                           Interceptor1
    │   ├── getModuleIds()                                                    Interceptor1
    │   ├── getModules()                                                      Interceptor1
    │   ├── getModulesDisabled()                                              Interceptor1
    │   ├── getNormalModuleIds()                                              Interceptor1
    │   ├── getNormalModules()                                                Interceptor1
    │   ├── getOverridingModuleIds()                                          Interceptor1
    │   ├── getOverridingModules()                                            Interceptor1
    │   └── getRelativelyInstalledBundles(Class<? extends Object>)            Interceptor1
    │
    ├── Service    (r.v.d.g.d.r.g.s.AopModule)
    │   └── somethingElse(List)                                               Interceptor1
    │
    └── Service2    (r.v.d.g.d.r.g.s.AopModule)
        └── somethingElse(List)                                               Interceptor1, Interceptor2
""" as String;
    }


    def "Check filter by interceptor render"() {

        expect:
        render(new GuiceAopConfig()
                .interceptors(AopModule.Interceptor2)) == """

    2 AOP handlers declared
    ├── AopModule/Interceptor1                                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule.configure(AopModule.java:23)
    └── AopModule/Interceptor2                                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.AopModule.configure(AopModule.java:24)


    1 bindings affected by AOP
    │
    └── Service2    (r.v.d.g.d.r.g.s.AopModule)
        └── somethingElse(List)                                               Interceptor1, Interceptor2
""" as String;
    }

    def "Check hide declarations block render"() {

        expect:
        render(new GuiceAopConfig()
                .hideDeclarationsBlock()
                .interceptors(AopModule.Interceptor2)) == """

    1 bindings affected by AOP
    │
    └── Service2    (r.v.d.g.d.r.g.s.AopModule)
        └── somethingElse(List)                                               Interceptor1, Interceptor2
""" as String;
    }


    def "Check empty report"() {

        expect:
        render(new GuiceAopConfig()
                .hideDeclarationsBlock()
                .types(Matchers.subclassesOf(AopModule.Service))
                .interceptors(AopModule.Interceptor2)).isEmpty()
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new AopModule())
                    .printGuiceAopMap()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    String render(GuiceAopConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }
}
