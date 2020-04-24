package ru.vyarus.dropwizard.guice.config.unique

import com.google.inject.AbstractModule
import com.google.inject.Inject
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticConfig
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticRenderer
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeConfig
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeRenderer
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 06.09.2019
 */
@UseGuiceyApp(App)
class Simple2DeduplicationReportTest extends AbstractTest {

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
        cleanupReport(renderer.renderReport(new DiagnosticConfig().printModules())) == """

    GUICE MODULES =
        Mod                          (r.v.d.g.c.u.Simple2DeduplicationReportTest) *REG(2/4)
        HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle)
        GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle)
        GuiceBootstrapModule         (r.v.d.guice.module)
""" as String;
    }

    def "Check configuration tree render"() {

        expect:
        cleanupReport(treeRenderer.renderReport(new ContextTreeConfig()
                .hideExtensions()
                .hideInstallers()
                .hideEmptyBundles()
                .hideScopes(ConfigScope.allExcept(ConfigScope.Application)))) == """

    APPLICATION
    ├── module     Mod                          (r.v.d.g.c.u.Simple2DeduplicationReportTest)
    ├── module     -Mod                         (r.v.d.g.c.u.Simple2DeduplicationReportTest) *DUPLICATE
    ├── module     Mod#2                        (r.v.d.g.c.u.Simple2DeduplicationReportTest)
    ├── module     -Mod#2                       (r.v.d.g.c.u.Simple2DeduplicationReportTest) *DUPLICATE
    └── module     GuiceBootstrapModule         (r.v.d.guice.module)
""" as String;
    }

    String cleanupReport(String report) {
        report.replaceAll("\r", "").replaceAll(" +\n", "\n")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new Mod(1), new Mod(1), new Mod(2), new Mod(2))
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Mod extends AbstractModule {

        int value;

        Mod(int value) {
            this.value = value
        }

        @Override
        boolean equals(Object obj) {
            return obj instanceof Mod && value.equals(obj.value)
        }

    }
}
