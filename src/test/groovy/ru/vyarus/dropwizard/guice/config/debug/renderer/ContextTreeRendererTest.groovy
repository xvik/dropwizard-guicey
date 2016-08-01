package ru.vyarus.dropwizard.guice.config.debug.renderer

import com.google.common.collect.Lists
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.context.debug.DiagnosticBundle
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeConfig
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeRenderer
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.support.util.GuiceRestrictedConfigBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 17.07.2016
 */
@UseGuiceyApp(App)
class ContextTreeRendererTest extends Specification {

    @Inject
    ContextTreeRenderer renderer

    def "Check full render"() {
        expect:
        render(new ContextTreeConfig()) == """

    APPLICATION
    ├── module     FooModule                    (r.v.d.g.d.s.features)
    ├── module     DiagnosticModule             (r.v.d.g.m.c.d.DiagnosticBundle)
    ├── module     GuiceSupportModule           (r.v.d.guice.module)
    ├── -disable   LifeCycleInstaller           (r.v.d.g.m.i.feature)
    │
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  LifeCycleInstaller           (r.v.d.g.m.i.feature)      *DISABLED
    │   ├── installer  ManagedInstaller             (r.v.d.g.m.i.feature)      *DISABLED
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
    │   ├── installer  JerseyProviderInstaller      (r.v.d.g.m.i.f.j.provider)
    │   ├── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
    │   ├── installer  EagerSingletonInstaller      (r.v.d.g.m.i.f.eager)
    │   ├── installer  HealthCheckInstaller         (r.v.d.g.m.i.f.health)
    │   ├── installer  TaskInstaller                (r.v.d.g.m.i.feature)
    │   ├── installer  PluginInstaller              (r.v.d.g.m.i.f.plugin)
    │   ├── installer  AdminFilterInstaller         (r.v.d.g.m.i.f.admin)
    │   └── installer  AdminServletInstaller        (r.v.d.g.m.i.f.admin)
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
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     *IGNORED
    │   ├── extension  HK2DebugFeature              (r.v.d.g.m.j.d.service)
    │   └── module     HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle)
    │
    ├── BUNDLES LOOKUP
    │   └── FooBundle                    (r.v.d.g.d.s.bundle)       *IGNORED
    │
    └── CLASSPATH SCAN
        ├── installer  FooInstaller                 (r.v.d.g.d.s.features)
        ├── extension  FooResource                  (r.v.d.g.d.s.features)
        ├── command    Cli                          (r.v.d.g.d.s.features)
        └── command    EnvCommand                   (r.v.d.g.d.s.features)
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
    │
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  LifeCycleInstaller           (r.v.d.g.m.i.feature)      *DISABLED
    │   ├── installer  ManagedInstaller             (r.v.d.g.m.i.feature)      *DISABLED
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
    │   ├── installer  JerseyProviderInstaller      (r.v.d.g.m.i.f.j.provider)
    │   ├── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
    │   ├── installer  EagerSingletonInstaller      (r.v.d.g.m.i.f.eager)
    │   ├── installer  HealthCheckInstaller         (r.v.d.g.m.i.f.health)
    │   ├── installer  TaskInstaller                (r.v.d.g.m.i.feature)
    │   ├── installer  PluginInstaller              (r.v.d.g.m.i.f.plugin)
    │   ├── installer  AdminFilterInstaller         (r.v.d.g.m.i.f.admin)
    │   └── installer  AdminServletInstaller        (r.v.d.g.m.i.f.admin)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── installer  FooBundleInstaller           (r.v.d.g.d.s.bundle)
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    │
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │   └── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     *IGNORED
    │
    ├── BUNDLES LOOKUP
    │   └── FooBundle                    (r.v.d.g.d.s.bundle)       *IGNORED
    │
    └── CLASSPATH SCAN
        └── installer  FooInstaller                 (r.v.d.g.d.s.features)
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
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │
    └── BUNDLES LOOKUP
        └── FooBundle                    (r.v.d.g.d.s.bundle)       *IGNORED
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
    │
    └── FooBundle                    (r.v.d.g.d.s.bundle)
        └── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
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
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   └── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │
    └── BUNDLES LOOKUP
        └── FooBundle                    (r.v.d.g.d.s.bundle)       *IGNORED
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
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
    │   ├── installer  JerseyProviderInstaller      (r.v.d.g.m.i.f.j.provider)
    │   ├── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
    │   ├── installer  EagerSingletonInstaller      (r.v.d.g.m.i.f.eager)
    │   ├── installer  HealthCheckInstaller         (r.v.d.g.m.i.f.health)
    │   ├── installer  TaskInstaller                (r.v.d.g.m.i.feature)
    │   ├── installer  PluginInstaller              (r.v.d.g.m.i.f.plugin)
    │   ├── installer  AdminFilterInstaller         (r.v.d.g.m.i.f.admin)
    │   └── installer  AdminServletInstaller        (r.v.d.g.m.i.f.admin)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── installer  FooBundleInstaller           (r.v.d.g.d.s.bundle)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    │
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │   └── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     *IGNORED
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
    │
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  LifeCycleInstaller           (r.v.d.g.m.i.feature)      *DISABLED
    │   ├── installer  ManagedInstaller             (r.v.d.g.m.i.feature)      *DISABLED
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
    │   ├── installer  JerseyProviderInstaller      (r.v.d.g.m.i.f.j.provider)
    │   ├── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
    │   ├── installer  EagerSingletonInstaller      (r.v.d.g.m.i.f.eager)
    │   ├── installer  HealthCheckInstaller         (r.v.d.g.m.i.f.health)
    │   ├── installer  TaskInstaller                (r.v.d.g.m.i.feature)
    │   ├── installer  PluginInstaller              (r.v.d.g.m.i.f.plugin)
    │   ├── installer  AdminFilterInstaller         (r.v.d.g.m.i.f.admin)
    │   └── installer  AdminServletInstaller        (r.v.d.g.m.i.f.admin)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── installer  FooBundleInstaller           (r.v.d.g.d.s.bundle)
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │
    └── CLASSPATH SCAN
        └── installer  FooInstaller                 (r.v.d.g.d.s.features)
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
    │
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
    │   └── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    │
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │   └── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     *IGNORED
    │
    └── BUNDLES LOOKUP
        └── FooBundle                    (r.v.d.g.d.s.bundle)       *IGNORED
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
    └── CoreInstallersBundle         (r.v.d.g.m.installer)
        ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
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
    │
    ├── FooBundle                    (r.v.d.g.d.s.bundle)
    │   ├── installer  FooBundleInstaller           (r.v.d.g.d.s.bundle)
    │   ├── -disable   ManagedInstaller             (r.v.d.g.m.i.feature)
    │   └── FooBundleRelativeBundle      (r.v.d.g.d.s.bundle)
    │
    ├── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
    ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
    │
    └── CLASSPATH SCAN
        └── installer  FooInstaller                 (r.v.d.g.d.s.features)
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
                            .bundles(
                            new FooBundle(),
                            new GuiceRestrictedConfigBundle())
                            .modules(new FooModule(), new DiagnosticBundle.DiagnosticModule())
                            .disableInstallers(LifeCycleInstaller)
                            .strictScopeControl()
                            .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}