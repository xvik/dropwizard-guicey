package ru.vyarus.dropwizard.guice.config.unique

import com.google.inject.AbstractModule
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticConfig
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticRenderer
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeConfig
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeRenderer
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 05.07.2019
 */
@UseGuiceyApp(App)
class DuplicateModulesReportingTest extends AbstractTest {

    @Inject
    DiagnosticRenderer renderer
    @Inject
    ContextTreeRenderer treeRenderer


    def "Check diagnostic info render"() {

        expect:
        cleanupReport(renderer.renderReport(new DiagnosticConfig().printDefaults())) == """

    BUNDLES =
        MiddleBundle                 (r.v.d.g.c.u.DuplicateModulesReportingTest)
        DiagnosticBundle             (r.v.d.g.m.c.debug)
        HK2DebugBundle               (r.v.d.g.m.j.debug)        *HOOK
        GuiceRestrictedConfigBundle  (r.v.d.g.support.util)     *HOOK
        CoreInstallersBundle         (r.v.d.g.m.installer)
            WebInstallersBundle          (r.v.d.g.m.installer)


    INSTALLERS and EXTENSIONS in processing order =
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(1/2)
            HK2DebugFeature              (r.v.d.g.m.j.d.service)


    GUICE MODULES =
        Module                       (r.v.d.g.c.u.DuplicateModulesReportingTest) *REG(5/8)
        HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle)
        GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle)
        DiagnosticModule             (r.v.d.g.m.c.d.DiagnosticBundle)
        GuiceBootstrapModule         (r.v.d.guice.module)
""" as String;
    }

    def "Check configuration tree render"() {

        expect:
        cleanupReport(treeRenderer.renderReport(new ContextTreeConfig())) == """

    APPLICATION
    ├── module     Module                       (r.v.d.g.c.u.DuplicateModulesReportingTest)
    ├── module     Module#2                     (r.v.d.g.c.u.DuplicateModulesReportingTest)
    ├── module     Module#2                     (r.v.d.g.c.u.DuplicateModulesReportingTest) *IGNORED
    ├── module     Module#3                     (r.v.d.g.c.u.DuplicateModulesReportingTest)
    ├── module     DiagnosticModule             (r.v.d.g.m.c.d.DiagnosticBundle)
    ├── module     GuiceBootstrapModule         (r.v.d.guice.module)
    │
    ├── MiddleBundle                 (r.v.d.g.c.u.DuplicateModulesReportingTest)
    │   ├── module     Module#2                     (r.v.d.g.c.u.DuplicateModulesReportingTest) *IGNORED
    │   ├── module     Module#4                     (r.v.d.g.c.u.DuplicateModulesReportingTest)
    │   ├── module     Module#4                     (r.v.d.g.c.u.DuplicateModulesReportingTest) *IGNORED
    │   └── module     Module#5                     (r.v.d.g.c.u.DuplicateModulesReportingTest)
    │
    ├── DiagnosticBundle             (r.v.d.g.m.c.debug)
    │
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)     *IGNORED
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
            def module = new Module(2)
            bootstrap.addBundle(GuiceBundle.builder()
            // 1 duplicate
                    .modules(new Module(1), module, new Module(2), new Module(3))
                    .bundles(new MiddleBundle(module))
                    .printDiagnosticInfo()
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class MiddleBundle implements GuiceyBundle {

        Module module

        MiddleBundle(Module module) {
            this.module = module
        }

        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            // 1 duplicate and 1 same instance
            bootstrap.modules(module, new Module(4), new Module(4), new Module(5))
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
}
