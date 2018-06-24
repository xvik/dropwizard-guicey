package ru.vyarus.dropwizard.guice.config.debug.renderer

import com.google.common.collect.Lists
import com.google.inject.Binder
import com.google.inject.Module
import io.dropwizard.Application
import io.dropwizard.Bundle
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.context.debug.DiagnosticBundle
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticConfig
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticRenderer
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.support.util.GuiceRestrictedConfigBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject
import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 16.07.2016
 */
@UseGuiceyApp(value = App, hooks = XConf)
class DiagnosticRendererTest extends Specification {

    @Inject
    DiagnosticRenderer renderer

    def "Check default render"() {

        expect:
        render(new DiagnosticConfig().printDefaults()) == """

    COMMANDS =
        Cli                          (r.v.d.g.d.s.features)     *SCAN
        EnvCommand                   (r.v.d.g.d.s.features)     *SCAN, GUICE_ENABLED


    BUNDLES =
        FooBundle                    (r.v.d.g.d.s.bundle)       *LOOKUP, REG(2)
            FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
        GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
        HK2DebugBundle               (r.v.d.g.m.j.debug)
        XBundle                      (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK
        CoreInstallersBundle         (r.v.d.g.m.installer)
        DWBundle                     (r.v.d.g.c.d.r.DiagnosticRendererTest) *DW


    INSTALLERS and EXTENSIONS in processing order =
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(2)
            HK2DebugFeature              (r.v.d.g.m.j.d.service)
        resource             (r.v.d.g.m.i.f.j.ResourceInstaller)
            LazyExtension                (r.v.d.g.c.d.r.DiagnosticRendererTest) *LAZY
            HKExtension                  (r.v.d.g.c.d.r.DiagnosticRendererTest) *HK2
            FooBundleResource            (r.v.d.g.d.s.bundle)
            FooResource                  (r.v.d.g.d.s.features)     *SCAN
        eagersingleton       (r.v.d.g.m.i.f.e.EagerSingletonInstaller)
            XExt                         (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK


    GUICE MODULES =
        FooModule                    (r.v.d.g.d.s.features)
        DiagnosticModule             (r.v.d.g.m.c.d.DiagnosticBundle)
        OverrideModule               (r.v.d.g.c.d.r.DiagnosticRendererTest) *OVERRIDE
        XMod                         (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK
        FooBundleModule              (r.v.d.g.d.s.bundle)
        GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle)
        HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle)
        GuiceBootstrapModule         (r.v.d.guice.module)
""" as String;
    }

    def "Check all render"() {

        expect:
        render(new DiagnosticConfig().printAll()) == """

    COMMANDS =
        Cli                          (r.v.d.g.d.s.features)     *SCAN
        EnvCommand                   (r.v.d.g.d.s.features)     *SCAN, GUICE_ENABLED


    BUNDLES =
        FooBundle                    (r.v.d.g.d.s.bundle)       *LOOKUP, REG(2)
            FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
        GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
        HK2DebugBundle               (r.v.d.g.m.j.debug)
        XBundle                      (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK
        CoreInstallersBundle         (r.v.d.g.m.installer)
        DWBundle                     (r.v.d.g.c.d.r.DiagnosticRendererTest) *DW
        -DisabledBundle              (r.v.d.g.c.d.r.DiagnosticRendererTest)


    INSTALLERS and EXTENSIONS in processing order =
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(2)
            HK2DebugFeature              (r.v.d.g.m.j.d.service)
        jerseyprovider       (r.v.d.g.m.i.f.j.p.JerseyProviderInstaller)
        resource             (r.v.d.g.m.i.f.j.ResourceInstaller)
            LazyExtension                (r.v.d.g.c.d.r.DiagnosticRendererTest) *LAZY
            HKExtension                  (r.v.d.g.c.d.r.DiagnosticRendererTest) *HK2
            FooBundleResource            (r.v.d.g.d.s.bundle)
            FooResource                  (r.v.d.g.d.s.features)     *SCAN
        eagersingleton       (r.v.d.g.m.i.f.e.EagerSingletonInstaller)
            XExt                         (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK
        healthcheck          (r.v.d.g.m.i.f.h.HealthCheckInstaller)
        task                 (r.v.d.g.m.i.feature.TaskInstaller)
        plugin               (r.v.d.g.m.i.f.plugin.PluginInstaller)
        foobundle            (r.v.d.g.d.s.b.FooBundleInstaller)
        foo                  (r.v.d.g.d.s.features.FooInstaller)    *SCAN
        -lifecycle           (r.v.d.g.m.i.f.LifeCycleInstaller)
        -managed             (r.v.d.g.m.i.feature.ManagedInstaller)


    DISABLED EXTENSIONS =
        -DisabledExtension           (r.v.d.g.c.d.r.DiagnosticRendererTest)


    GUICE MODULES =
        FooModule                    (r.v.d.g.d.s.features)
        DiagnosticModule             (r.v.d.g.m.c.d.DiagnosticBundle)
        OverrideModule               (r.v.d.g.c.d.r.DiagnosticRendererTest) *OVERRIDE
        XMod                         (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK
        FooBundleModule              (r.v.d.g.d.s.bundle)
        GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle)
        HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle)
        GuiceBootstrapModule         (r.v.d.guice.module)
        -DisabledModule              (r.v.d.g.c.d.r.DiagnosticRendererTest)
""" as String;
    }

    def "Check bundles render"() {

        expect:
        render(new DiagnosticConfig().printBundles()) == """

    BUNDLES =
        FooBundle                    (r.v.d.g.d.s.bundle)       *LOOKUP, REG(2)
            FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
        GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
        HK2DebugBundle               (r.v.d.g.m.j.debug)
        XBundle                      (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK
        CoreInstallersBundle         (r.v.d.g.m.installer)
        DWBundle                     (r.v.d.g.c.d.r.DiagnosticRendererTest) *DW
""" as String;
    }

    def "Check disabled bundles render"() {

        expect:
        render(new DiagnosticConfig().printBundles().printDisabledItems()) == """

    BUNDLES =
        FooBundle                    (r.v.d.g.d.s.bundle)       *LOOKUP, REG(2)
            FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
        GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
        HK2DebugBundle               (r.v.d.g.m.j.debug)
        XBundle                      (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK
        CoreInstallersBundle         (r.v.d.g.m.installer)
        DWBundle                     (r.v.d.g.c.d.r.DiagnosticRendererTest) *DW
        -DisabledBundle              (r.v.d.g.c.d.r.DiagnosticRendererTest)
""" as String;
    }

    def "Check installers render"() {

        expect:
        render(new DiagnosticConfig().printInstallers()) == """

    INSTALLERS in processing order =
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(2)
        resource             (r.v.d.g.m.i.f.j.ResourceInstaller)
        eagersingleton       (r.v.d.g.m.i.f.e.EagerSingletonInstaller)
""" as String;
    }

    def "Check all installers render"() {

        expect:
        render(new DiagnosticConfig()
                .printInstallers()
                .printNotUsedInstallers()) == """

    INSTALLERS in processing order =
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(2)
        jerseyprovider       (r.v.d.g.m.i.f.j.p.JerseyProviderInstaller)
        resource             (r.v.d.g.m.i.f.j.ResourceInstaller)
        eagersingleton       (r.v.d.g.m.i.f.e.EagerSingletonInstaller)
        healthcheck          (r.v.d.g.m.i.f.h.HealthCheckInstaller)
        task                 (r.v.d.g.m.i.feature.TaskInstaller)
        plugin               (r.v.d.g.m.i.f.plugin.PluginInstaller)
        foobundle            (r.v.d.g.d.s.b.FooBundleInstaller)
        foo                  (r.v.d.g.d.s.features.FooInstaller)    *SCAN
""" as String;
    }

    def "Check disabled installers render"() {

        expect:
        render(new DiagnosticConfig()
                .printInstallers()
                .printDisabledItems()) == """

    INSTALLERS in processing order =
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(2)
        resource             (r.v.d.g.m.i.f.j.ResourceInstaller)
        eagersingleton       (r.v.d.g.m.i.f.e.EagerSingletonInstaller)
        -lifecycle           (r.v.d.g.m.i.f.LifeCycleInstaller)
        -managed             (r.v.d.g.m.i.feature.ManagedInstaller)
""" as String;
    }

    def "Check installers with extensions render"() {

        expect:
        render(new DiagnosticConfig()
                .printInstallers()
                .printExtensions()) == """

    INSTALLERS and EXTENSIONS in processing order =
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(2)
            HK2DebugFeature              (r.v.d.g.m.j.d.service)
        resource             (r.v.d.g.m.i.f.j.ResourceInstaller)
            LazyExtension                (r.v.d.g.c.d.r.DiagnosticRendererTest) *LAZY
            HKExtension                  (r.v.d.g.c.d.r.DiagnosticRendererTest) *HK2
            FooBundleResource            (r.v.d.g.d.s.bundle)
            FooResource                  (r.v.d.g.d.s.features)     *SCAN
        eagersingleton       (r.v.d.g.m.i.f.e.EagerSingletonInstaller)
            XExt                         (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK
""" as String;
    }

    def "Check extensions render"() {

        expect:
        render(new DiagnosticConfig()
                .printExtensions()) == """

    EXTENSIONS =
        LazyExtension                (r.v.d.g.c.d.r.DiagnosticRendererTest) *LAZY
        HKExtension                  (r.v.d.g.c.d.r.DiagnosticRendererTest) *HK2
        XExt                         (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK
        FooBundleResource            (r.v.d.g.d.s.bundle)
        HK2DebugFeature              (r.v.d.g.m.j.d.service)
        FooResource                  (r.v.d.g.d.s.features)     *SCAN
""" as String;
    }

    def "Check disabled extensions render"() {

        expect:
        render(new DiagnosticConfig()
                .printExtensions().printDisabledItems()) == """

    EXTENSIONS =
        LazyExtension                (r.v.d.g.c.d.r.DiagnosticRendererTest) *LAZY
        HKExtension                  (r.v.d.g.c.d.r.DiagnosticRendererTest) *HK2
        XExt                         (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK
        FooBundleResource            (r.v.d.g.d.s.bundle)
        HK2DebugFeature              (r.v.d.g.m.j.d.service)
        FooResource                  (r.v.d.g.d.s.features)     *SCAN
        -DisabledExtension           (r.v.d.g.c.d.r.DiagnosticRendererTest)
""" as String;
    }

    def "Check modules render"() {

        expect:
        render(new DiagnosticConfig()
                .printModules()) == """

    GUICE MODULES =
        FooModule                    (r.v.d.g.d.s.features)
        DiagnosticModule             (r.v.d.g.m.c.d.DiagnosticBundle)
        OverrideModule               (r.v.d.g.c.d.r.DiagnosticRendererTest) *OVERRIDE
        XMod                         (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK
        FooBundleModule              (r.v.d.g.d.s.bundle)
        GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle)
        HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle)
        GuiceBootstrapModule         (r.v.d.guice.module)
""" as String;
    }


    def "Check disabled modules render"() {

        expect:
        render(new DiagnosticConfig()
                .printModules().printDisabledItems()) == """

    GUICE MODULES =
        FooModule                    (r.v.d.g.d.s.features)
        DiagnosticModule             (r.v.d.g.m.c.d.DiagnosticBundle)
        OverrideModule               (r.v.d.g.c.d.r.DiagnosticRendererTest) *OVERRIDE
        XMod                         (r.v.d.g.c.d.r.DiagnosticRendererTest) *HOOK
        FooBundleModule              (r.v.d.g.d.s.bundle)
        GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle)
        HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle)
        GuiceBootstrapModule         (r.v.d.guice.module)
        -DisabledModule              (r.v.d.g.c.d.r.DiagnosticRendererTest)
""" as String;
    }


    String render(DiagnosticConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(new DWBundle())
            bootstrap.addBundle(
                    GuiceBundle.builder()
                            .bundleLookup(new GuiceyBundleLookup() {
                        @Override
                        List<GuiceyBundle> lookup() {
                            return Lists.asList(new FooBundle())
                        }
                    })
                            .enableAutoConfig(FooResource.package.name)
                            .searchCommands()
                            .bundles(new FooBundle(), new GuiceRestrictedConfigBundle(), new DisabledBundle())
                            .modules(new FooModule(), new DiagnosticBundle.DiagnosticModule(), new DisabledModule())
                            .modulesOverride(new OverrideModule())
                            .extensions(LazyExtension, HKExtension, DisabledExtension)
                            .disableInstallers(LifeCycleInstaller)
                            .disableBundles(DisabledBundle)
                            .disableModules(DisabledModule)
                            .disableExtensions(DisabledExtension)
                            .configureFromDropwizardBundles()
                            .strictScopeControl()
                            .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class DWBundle implements Bundle, GuiceyBundle {
        @Override
        void initialize(Bootstrap<?> bootstrap) {

        }

        @Override
        void run(Environment environment) {

        }

        @Override
        void initialize(GuiceyBootstrap bootstrap) {

        }
    }

    @LazyBinding
    @Path("/lazy")
    static class LazyExtension {}

    @HK2Managed
    @Path("/hk")
    static class HKExtension {}

    static class DisabledBundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            // must not appear at all
            bootstrap.bundles(new DisabledSubBundle())
        }
    }

    static class DisabledSubBundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {

        }
    }

    static class DisabledModule implements Module {
        @Override
        void configure(Binder binder) {

        }
    }

    @Path("/")
    static class DisabledExtension {}

    static class OverrideModule implements Module {
        @Override
        void configure(Binder binder) {
        }
    }

    static class XConf implements GuiceyConfigurationHook {

        @Override
        void configure(GuiceBundle.Builder builder) {
            builder.modules(new XMod())
                    .bundles(new XBundle())
                    .extensions(XExt)
        }
    }

    static class XMod implements Module {
        @Override
        void configure(Binder binder) {

        }
    }

    static class XBundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {

        }
    }

    @EagerSingleton
    static class XExt {
    }
}