package ru.vyarus.dropwizard.guice.config.unique

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.ConfiguredBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
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
 * @since 28.07.2019
 */
@UseGuiceyApp(App)
class DuplicateDropwizardBundlesReportingTest extends AbstractTest {

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
        cleanupReport(renderer.renderReport(new DiagnosticConfig().printDefaults())) == """

    BUNDLES =
        DBundle                      (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest) *DW, REG(7/12)
        MiddleBundle                 (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest)
        HK2DebugBundle               (r.v.d.g.m.j.debug)        *HOOK
        GuiceRestrictedConfigBundle  (r.v.d.g.support.util)     *HOOK
        CoreInstallersBundle         (r.v.d.g.m.installer)
            WebInstallersBundle          (r.v.d.g.m.installer)


    INSTALLERS and EXTENSIONS in processing order =
        jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller) *REG(1/2)
            HK2DebugFeature              (r.v.d.g.m.j.d.service)


    GUICE MODULES =
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
    ├── DBundle                      (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest) *DW
    ├── -DBundle                     (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest) *DW, IGNORED(2)
    ├── DBundle#2                    (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest) *DW
    │
    ├── DBundle#3                    (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest) *DW
    │   └── DBundle#4                    (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest) *DW
    │
    ├── MiddleBundle                 (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest)
    │   ├── -DBundle#2                   (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest) *DW, IGNORED
    │   │
    │   ├── DBundle#5                    (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest) *DW
    │   │   └── DBundle#6                    (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest) *DW
    │   │
    │   ├── -DBundle#5                   (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest) *DW, IGNORED(2)
    │   └── DBundle#7                    (r.v.d.g.c.u.DuplicateDropwizardBundlesReportingTest) *DW
    │
    ├── CoreInstallersBundle         (r.v.d.g.m.installer)
    │   ├── installer  -JerseyFeatureInstaller      (r.v.d.g.m.i.f.jersey)     *IGNORED
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
                    .dropwizardBundles(new DBundle(1), new DBundle(1), new DBundle(2), new DBundle(3), new DBundle(1))
                    .bundles(new MiddleBundle())
                    .printDiagnosticInfo()
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    static class DBundle implements ConfiguredBundle {

        int num

        DBundle(int num) {
            this.num = num
        }

        @Override
        void initialize(Bootstrap bootstrap) {
            if (num == 3 || num == 4) {
                bootstrap.addBundle(new DBundle(num * 10))
            }
        }

        boolean equals(o) {
            return o instanceof DBundle && num == o.num
        }
    }

    static class MiddleBundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {
            bootstrap.dropwizardBundles(new DBundle(2), new DBundle(4), new DBundle(4), new DBundle(5), new DBundle(4))
        }
    }
}
