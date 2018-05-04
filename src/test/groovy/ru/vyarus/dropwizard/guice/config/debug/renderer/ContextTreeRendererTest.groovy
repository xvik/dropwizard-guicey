package ru.vyarus.dropwizard.guice.config.debug.renderer

import com.google.common.collect.Lists
import com.google.inject.Binder
import com.google.inject.Module
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.context.Disables
import ru.vyarus.dropwizard.guice.module.context.debug.DiagnosticBundle
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeConfig
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeRenderer
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator
import ru.vyarus.dropwizard.guice.support.util.GuiceRestrictedConfigBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject
import javax.ws.rs.Path

import static ru.vyarus.dropwizard.guice.module.context.ConfigScope.Configurator
import static ru.vyarus.dropwizard.guice.module.context.ConfigScope.allExcept

/**
 * @author Vyacheslav Rusakov
 * @since 17.07.2016
 */
@UseGuiceyApp(value = App, configurators = DisableConfigurator)
class ContextTreeRendererTest extends Specification {

    @Inject
    ContextTreeRenderer renderer

    def "Check full render"() {
        expect:
        render(new ContextTreeConfig()) == """

    APPLICATION
    ├── extension  DisabledExtension            (r.v.d.g.c.d.r.ContextTreeRendererTest) *DISABLED
    ├── extension  DisabledExtension2           (r.v.d.g.c.d.r.ContextTreeRendererTest) *DISABLED
    ├── module     FooModule                    (r.v.d.g.d.s.features)
    ├── module     DiagnosticModule             (r.v.d.g.m.c.d.DiagnosticBundle)
    ├── module     DisabledModule               (r.v.d.g.c.d.r.ContextTreeRendererTest) *DISABLED
    ├── module     GuiceBootstrapModule         (r.v.d.guice.module)
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)
    ├── -disable   DisabledExtension2           (r.v.d.g.c.d.r.ContextTreeRendererTest)
    ├── -disable   DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── installer  FooBundleInstaller           (r.v.d.g.d.s.bundle)
    │   ├── extension  FooBundleResource            (r.v.d.g.d.s.bundle)
    │   ├── module     FooBundleModule              (r.v.d.g.d.s.bundle)
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    │   └── module     GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle)
    │
    ├── DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest) *DISABLED
    │
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
    │   ├── extension  HK2DebugFeature              (r.v.d.g.m.j.d.service)
    │   └── module     HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle)
    │
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     *IGNORED
    │   ├── installer  LifeCycleInstaller           (r.v.d.g.m.i.feature)      *DISABLED
    │   ├── installer  ManagedInstaller             (r.v.d.g.m.i.feature)      *DISABLED
    │   ├── installer  JerseyProviderInstaller      (r.v.d.g.m.i.f.j.provider)
    │   ├── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
    │   ├── installer  EagerSingletonInstaller      (r.v.d.g.m.i.f.eager)
    │   ├── installer  HealthCheckInstaller         (r.v.d.g.m.i.f.health)
    │   ├── installer  TaskInstaller                (r.v.d.g.m.i.feature)
    │   └── installer  PluginInstaller              (r.v.d.g.m.i.f.plugin)
    │
    ├── BUNDLES LOOKUP
    │   └── FooBundle                    (r.v.d.g.d.s.bundle)       *IGNORED
    │
    ├── CLASSPATH SCAN
    │   ├── installer  FooInstaller                 (r.v.d.g.d.s.features)
    │   ├── extension  FooResource                  (r.v.d.g.d.s.features)
    │   ├── command    Cli                          (r.v.d.g.d.s.features)
    │   └── command    EnvCommand                   (r.v.d.g.d.s.features)
    │
    └── CONFIGURATORS
        ├── -disable   DisabledExtension            (r.v.d.g.c.d.r.ContextTreeRendererTest)
        └── -disable   DisabledModule               (r.v.d.g.c.d.r.ContextTreeRendererTest)
"""
    }

    def "Check items hide render"() {
        expect:
        render(new ContextTreeConfig()
                .hideModules()
                .hideExtensions()
                .hideCommands()) == """

    APPLICATION
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)
    ├── -disable   DisabledExtension2           (r.v.d.g.c.d.r.ContextTreeRendererTest)
    ├── -disable   DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── installer  FooBundleInstaller           (r.v.d.g.d.s.bundle)
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    ├── DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest) *DISABLED
    │
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │   └── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
    │
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     *IGNORED
    │   ├── installer  LifeCycleInstaller           (r.v.d.g.m.i.feature)      *DISABLED
    │   ├── installer  ManagedInstaller             (r.v.d.g.m.i.feature)      *DISABLED
    │   ├── installer  JerseyProviderInstaller      (r.v.d.g.m.i.f.j.provider)
    │   ├── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
    │   ├── installer  EagerSingletonInstaller      (r.v.d.g.m.i.f.eager)
    │   ├── installer  HealthCheckInstaller         (r.v.d.g.m.i.f.health)
    │   ├── installer  TaskInstaller                (r.v.d.g.m.i.feature)
    │   └── installer  PluginInstaller              (r.v.d.g.m.i.f.plugin)
    │
    ├── BUNDLES LOOKUP
    │   └── FooBundle                    (r.v.d.g.d.s.bundle)       *IGNORED
    │
    ├── CLASSPATH SCAN
    │   └── installer  FooInstaller                 (r.v.d.g.d.s.features)
    │
    └── CONFIGURATORS
        ├── -disable   DisabledExtension            (r.v.d.g.c.d.r.ContextTreeRendererTest)
        └── -disable   DisabledModule               (r.v.d.g.c.d.r.ContextTreeRendererTest)
"""
    }

    def "Check all items hide render"() {
        expect:
        render(new ContextTreeConfig()
                .hideModules()
                .hideExtensions()
                .hideInstallers()
                .hideCommands()) == """

    APPLICATION
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)
    ├── -disable   DisabledExtension2           (r.v.d.g.c.d.r.ContextTreeRendererTest)
    ├── -disable   DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    ├── DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest) *DISABLED
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │
    ├── BUNDLES LOOKUP
    │   └── FooBundle                    (r.v.d.g.d.s.bundle)       *IGNORED
    │
    └── CONFIGURATORS
        ├── -disable   DisabledExtension            (r.v.d.g.c.d.r.ContextTreeRendererTest)
        └── -disable   DisabledModule               (r.v.d.g.c.d.r.ContextTreeRendererTest)
"""
    }

    def "Check hide empty bundles render"() {
        expect:
        render(new ContextTreeConfig()
                .hideModules()
                .hideExtensions()
                .hideInstallers()
                .hideEmptyBundles()
                .hideDuplicateRegistrations()
                .hideCommands()) == """

    APPLICATION
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)
    ├── -disable   DisabledExtension2           (r.v.d.g.c.d.r.ContextTreeRendererTest)
    ├── -disable   DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   └── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │
    └── CONFIGURATORS
        ├── -disable   DisabledExtension            (r.v.d.g.c.d.r.ContextTreeRendererTest)
        └── -disable   DisabledModule               (r.v.d.g.c.d.r.ContextTreeRendererTest)
"""
    }

    def "Check duplicate bundle render even when empty bundles hidden"() {
        expect:
        render(new ContextTreeConfig()
                .hideModules()
                .hideExtensions()
                .hideInstallers()
                .hideEmptyBundles()
                .hideCommands()) == """

    APPLICATION
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)
    ├── -disable   DisabledExtension2           (r.v.d.g.c.d.r.ContextTreeRendererTest)
    ├── -disable   DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   └── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │
    ├── BUNDLES LOOKUP
    │   └── FooBundle                    (r.v.d.g.d.s.bundle)       *IGNORED
    │
    └── CONFIGURATORS
        ├── -disable   DisabledExtension            (r.v.d.g.c.d.r.ContextTreeRendererTest)
        └── -disable   DisabledModule               (r.v.d.g.c.d.r.ContextTreeRendererTest)
"""
    }

    def "Check disable hide render"() {
        expect:
        render(new ContextTreeConfig()
                .hideModules()
                .hideExtensions()
                .hideDisables()
                .hideCommands()) == """

    APPLICATION
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── installer  FooBundleInstaller           (r.v.d.g.d.s.bundle)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    │
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │   └── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
    │
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     *IGNORED
    │   ├── installer  JerseyProviderInstaller      (r.v.d.g.m.i.f.j.provider)
    │   ├── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
    │   ├── installer  EagerSingletonInstaller      (r.v.d.g.m.i.f.eager)
    │   ├── installer  HealthCheckInstaller         (r.v.d.g.m.i.f.health)
    │   ├── installer  TaskInstaller                (r.v.d.g.m.i.feature)
    │   └── installer  PluginInstaller              (r.v.d.g.m.i.f.plugin)
    │
    ├── BUNDLES LOOKUP
    │   └── FooBundle                    (r.v.d.g.d.s.bundle)       *IGNORED
    │
    └── CLASSPATH SCAN
        └── installer  FooInstaller                 (r.v.d.g.d.s.features)
"""
    }

    def "Check hide duplicate registrations render"() {
        expect:
        render(new ContextTreeConfig()
                .hideModules()
                .hideExtensions()
                .hideDuplicateRegistrations()
                .hideCommands()) == """

    APPLICATION
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)
    ├── -disable   DisabledExtension2           (r.v.d.g.c.d.r.ContextTreeRendererTest)
    ├── -disable   DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── installer  FooBundleInstaller           (r.v.d.g.d.s.bundle)
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    ├── DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest) *DISABLED
    │
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │   └── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
    │
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  LifeCycleInstaller           (r.v.d.g.m.i.feature)      *DISABLED
    │   ├── installer  ManagedInstaller             (r.v.d.g.m.i.feature)      *DISABLED
    │   ├── installer  JerseyProviderInstaller      (r.v.d.g.m.i.f.j.provider)
    │   ├── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
    │   ├── installer  EagerSingletonInstaller      (r.v.d.g.m.i.f.eager)
    │   ├── installer  HealthCheckInstaller         (r.v.d.g.m.i.f.health)
    │   ├── installer  TaskInstaller                (r.v.d.g.m.i.feature)
    │   └── installer  PluginInstaller              (r.v.d.g.m.i.f.plugin)
    │
    ├── CLASSPATH SCAN
    │   └── installer  FooInstaller                 (r.v.d.g.d.s.features)
    │
    └── CONFIGURATORS
        ├── -disable   DisabledExtension            (r.v.d.g.c.d.r.ContextTreeRendererTest)
        └── -disable   DisabledModule               (r.v.d.g.c.d.r.ContextTreeRendererTest)
"""
    }

    def "Check hide not used installers render"() {
        expect:
        render(new ContextTreeConfig()
                .hideModules()
                .hideExtensions()
                .hideNotUsedInstallers()
                .hideCommands()) == """

    APPLICATION
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)
    ├── -disable   DisabledExtension2           (r.v.d.g.c.d.r.ContextTreeRendererTest)
    ├── -disable   DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    ├── DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest) *DISABLED
    │
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │   └── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
    │
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     *IGNORED
    │   └── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
    │
    ├── BUNDLES LOOKUP
    │   └── FooBundle                    (r.v.d.g.d.s.bundle)       *IGNORED
    │
    └── CONFIGURATORS
        ├── -disable   DisabledExtension            (r.v.d.g.c.d.r.ContextTreeRendererTest)
        └── -disable   DisabledModule               (r.v.d.g.c.d.r.ContextTreeRendererTest)
"""
    }

    def "Check only used installers render"() {
        expect:
        render(new ContextTreeConfig()
                .hideModules()
                .hideExtensions()
                .hideNotUsedInstallers()
                .hideEmptyBundles()
                .hideDisables()
                .hideDuplicateRegistrations()
                .hideCommands()) == """

    APPLICATION
    │
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │   └── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
    │
    └── CoreInstallersBundle         (r.v.d.g.m.installer)
        └── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
"""
    }

    def "Check scope hide render"() {
        expect:
        render(new ContextTreeConfig()
                .hideModules()
                .hideExtensions()
                .hideDuplicateRegistrations()
                .hideScopes(CoreInstallersBundle)
                .hideCommands()) == """

    APPLICATION
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)
    ├── -disable   DisabledExtension2           (r.v.d.g.c.d.r.ContextTreeRendererTest)
    ├── -disable   DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── installer  FooBundleInstaller           (r.v.d.g.d.s.bundle)
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    ├── DisabledBundle               (r.v.d.g.c.d.r.ContextTreeRendererTest) *DISABLED
    │
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │   └── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
    │
    ├── CLASSPATH SCAN
    │   └── installer  FooInstaller                 (r.v.d.g.d.s.features)
    │
    └── CONFIGURATORS
        ├── -disable   DisabledExtension            (r.v.d.g.c.d.r.ContextTreeRendererTest)
        └── -disable   DisabledModule               (r.v.d.g.c.d.r.ContextTreeRendererTest)
"""
    }


    def "Check configurators exclusive render"() {
        expect:
        render(new ContextTreeConfig()
                .hideScopes(allExcept(Configurator))) == """

    APPLICATION
    │
    └── CONFIGURATORS
        ├── -disable   DisabledExtension            (r.v.d.g.c.d.r.ContextTreeRendererTest)
        └── -disable   DisabledModule               (r.v.d.g.c.d.r.ContextTreeRendererTest)
"""
    }

    String render(ContextTreeConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
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
                            .extensions(DisabledExtension, DisabledExtension2)
                            .disableInstallers(LifeCycleInstaller)
                            .disableBundles(DisabledBundle)
                            .disable(Disables.type(DisabledExtension2))
                            .strictScopeControl()
                            .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class DisabledBundle implements GuiceyBundle {
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

    static class DisableConfigurator implements GuiceyConfigurator {
        @Override
        void configure(GuiceBundle.Builder builder) {
            builder
                    .disableExtensions(DisabledExtension)
                    .disableModules(DisabledModule)
        }
    }

    @Path("/")
    static class DisabledExtension2 {}
}