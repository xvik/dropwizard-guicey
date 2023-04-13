package ru.vyarus.dropwizard.guice.config.unique

import com.google.inject.AbstractModule
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticConfig
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticRenderer
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeConfig
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeRenderer
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import jakarta.inject.Inject
import jakarta.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 08.07.2019
 */
@TestGuiceyApp(App)
class DuplicateScopesReportingTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    DiagnosticRenderer renderer
    ContextTreeRenderer treeRenderer

    void setup() {
        renderer = new DiagnosticRenderer(info)
        treeRenderer = new ContextTreeRenderer(info)
    }

    def "Check diagnostic info render"() {

        expect:
        Bundle.init == 5
        Bundle.run == 5
        cleanupReport(renderer.renderReport(new DiagnosticConfig().printDefaults())) == """

    BUNDLES =
        Bundle                       (r.v.d.g.c.u.DuplicateScopesReportingTest) *REG(5/8)
        MiddleBundle                 (r.v.d.g.c.u.DuplicateScopesReportingTest)
        HK2DebugBundle               (r.v.d.g.m.j.debug)        *HOOK
        GuiceRestrictedConfigBundle  (r.v.d.g.support.util)     *HOOK
        CoreInstallersBundle         (r.v.d.g.m.installer)
            WebInstallersBundle          (r.v.d.g.m.installer)


    INSTALLERS and EXTENSIONS in processing order =
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(1/2)
            HK2DebugFeature              (r.v.d.g.m.j.d.service)
        resource             (r.v.d.g.m.i.f.j.ResourceInstaller)
            Ext                          (r.v.d.g.c.u.DuplicateScopesReportingTest) *REG(1/5)


    GUICE MODULES =
        Module                       (r.v.d.g.c.u.DuplicateScopesReportingTest) *REG(5/24)
        HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle)
        GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle)
        GuiceBootstrapModule         (r.v.d.guice.module)
""" as String;
    }

    def "Check configuration tree render"() {

        expect:
        cleanupReport(treeRenderer.renderReport(new ContextTreeConfig())) == """

    APPLICATION
    ├── module     GuiceBootstrapModule         (r.v.d.guice.module)
    │
    ├── Bundle                       (r.v.d.g.c.u.DuplicateScopesReportingTest)
    │   ├── extension  Ext                          (r.v.d.g.c.u.DuplicateScopesReportingTest)
    │   ├── module     Module                       (r.v.d.g.c.u.DuplicateScopesReportingTest)
    │   ├── module     -Module                      (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │   ├── module     Module#2                     (r.v.d.g.c.u.DuplicateScopesReportingTest)
    │   └── module     Module#3                     (r.v.d.g.c.u.DuplicateScopesReportingTest)
    │
    ├── -Bundle                      (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │
    ├── MiddleBundle                 (r.v.d.g.c.u.DuplicateScopesReportingTest)
    │   ├── module     -Module#2                    (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │   ├── module     Module#4                     (r.v.d.g.c.u.DuplicateScopesReportingTest)
    │   ├── module     -Module#4                    (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │   ├── module     Module#5                     (r.v.d.g.c.u.DuplicateScopesReportingTest)
    │   ├── -Bundle#2                    (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │   │
    │   ├── Bundle#4                     (r.v.d.g.c.u.DuplicateScopesReportingTest)
    │   │   ├── extension  -Ext                         (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │   │   ├── module     -Module                      (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE(2)
    │   │   ├── module     -Module#2                    (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │   │   └── module     -Module#3                    (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │   │
    │   ├── -Bundle#4                    (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │   │
    │   └── Bundle#5                     (r.v.d.g.c.u.DuplicateScopesReportingTest)
    │       ├── extension  -Ext                         (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │       ├── module     -Module                      (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE(2)
    │       ├── module     -Module#2                    (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │       └── module     -Module#3                    (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │
    ├── Bundle#2                     (r.v.d.g.c.u.DuplicateScopesReportingTest)
    │   ├── extension  -Ext                         (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │   ├── module     -Module                      (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE(2)
    │   ├── module     -Module#2                    (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │   └── module     -Module#3                    (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │
    ├── Bundle#3                     (r.v.d.g.c.u.DuplicateScopesReportingTest)
    │   ├── extension  -Ext                         (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │   ├── module     -Module                      (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE(2)
    │   ├── module     -Module#2                    (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │   └── module     -Module#3                    (r.v.d.g.c.u.DuplicateScopesReportingTest) *DUPLICATE
    │
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  -JerseyFeatureInstaller      (r.v.d.g.m.i.f.jersey)     *DUPLICATE
    │   ├── installer  LifeCycleInstaller           (r.v.d.g.m.i.feature)
    │   ├── installer  ManagedInstaller             (r.v.d.g.m.i.feature)
    │   ├── installer  JerseyProviderInstaller      (r.v.d.g.m.i.f.j.provider)
    │   ├── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
    │   ├── installer  EagerSingletonInstaller      (r.v.d.g.m.i.f.eager)
    │   ├── installer  HealthCheckInstaller         (r.v.d.g.m.i.f.health)
    │   ├── installer  TaskInstaller                (r.v.d.g.m.i.feature)
    │   ├── installer  PluginInstaller              (r.v.d.g.m.i.f.plugin)
    │   │
    │   └── WebInstallersBundle          (r.v.d.g.m.installer)
    │       ├── installer  WebFilterInstaller           (r.v.d.g.m.i.f.web)
    │       ├── installer  WebServletInstaller          (r.v.d.g.m.i.f.web)
    │       └── installer  WebListenerInstaller         (r.v.d.g.m.i.f.w.listener)
    │
    └── HOOKS
        │
        ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
        │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
        │   ├── extension  HK2DebugFeature              (r.v.d.g.m.j.d.service)
        │   └── module     HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle)
        │
        └── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
            └── module     GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle)
""" as String;
    }

    String cleanupReport(String report) {
        report.replaceAll("\r", "").replaceAll(" +\n", "\n")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
            // 1 duplicate
                    .bundles(new Bundle(1), new Bundle(1), new MiddleBundle(), new Bundle(2), new Bundle(3))
                    .printDiagnosticInfo()
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class MiddleBundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            // 2 duplicates
            bootstrap.bundles(new Bundle(2), new Bundle(4), new Bundle(4), new Bundle(5))
                    .modules(new Module(2), new Module(4), new Module(4), new Module(5))
        }
    }

    static class Bundle implements GuiceyBundle {

        static init = 0
        static run = 0

        int value;

        Bundle(int value) {
            this.value = value
        }

        @Override
        boolean equals(Object obj) {
            return obj instanceof Bundle && value.equals(obj.value)
        }

        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            init++
            bootstrap
                    .modules(new Module(1), new Module(1), new Module(2), new Module(3))
                    .extensions(Ext)
        }

        @Override
        void run(GuiceyEnvironment environment) {
            run++
        }
    }

    static class Module extends AbstractModule {

        int value;

        Module(int value) {
            this.value = value
        }

        @Override
        boolean equals(Object obj) {
            return obj instanceof Module && value.equals(obj.value)
        }
    }

    @Path("/some")
    static class Ext {}
}
