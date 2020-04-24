package ru.vyarus.dropwizard.guice.config.unique

import com.google.inject.AbstractModule
import com.google.inject.Inject
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeConfig
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeRenderer
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 06.09.2019
 */
@UseGuiceyApp(App)
class TreeReportInstancesOrderTest extends AbstractTest {


    @Inject
    GuiceyConfigurationInfo info
    ContextTreeRenderer treeRenderer

    void setup() {
        treeRenderer = new ContextTreeRenderer(info)
    }

    def "Check configuration tree render"() {

        expect:
        cleanupReport(treeRenderer.renderReport(new ContextTreeConfig()
                .hideInstallers()
                .hideScopes(ConfigScope.allExcept(ConfigScope.Application))
                .hideScopes(CoreInstallersBundle))) == """

    APPLICATION
    ├── extension  Ext1                         (r.v.d.g.c.u.TreeReportInstancesOrderTest)
    ├── extension  Ext2                         (r.v.d.g.c.u.TreeReportInstancesOrderTest)
    ├── module     Mod                          (r.v.d.g.c.u.TreeReportInstancesOrderTest)
    ├── module     -Mod                         (r.v.d.g.c.u.TreeReportInstancesOrderTest) *DUPLICATE
    ├── module     OtherMod                     (r.v.d.g.c.u.TreeReportInstancesOrderTest)
    ├── module     Mod#2                        (r.v.d.g.c.u.TreeReportInstancesOrderTest)
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
                    .extensions(Ext1.class)
                    .modules(new Mod(1), new OtherMod())
                    .extensions(Ext2.class)
                    .modules(new Mod(2), new Mod(1))
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

    static class OtherMod extends AbstractModule {}

    @EagerSingleton
    static class Ext1 {}

    @EagerSingleton
    static class Ext2 {}
}
