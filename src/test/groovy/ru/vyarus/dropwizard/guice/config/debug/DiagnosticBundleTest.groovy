package ru.vyarus.dropwizard.guice.config.debug

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.Foo2Bundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundleResource
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.context.debug.DiagnosticBundle
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticConfig
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeConfig
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 12.07.2016
 */
@UseDropwizardApp(App)
// important to track HK part also
class DiagnosticBundleTest extends AbstractTest {

    def "Check logging"() {

        // test checks just no exceptions
        // used for real reporting manual testing

        expect: "correct startup"
        true

    }

    def "Check empty config detection"() {

        when: "empty config provided"
        DiagnosticBundle.builder()
                .printConfiguration(new DiagnosticConfig())

        then: "error"
        thrown(IllegalStateException)

    }

    def "Diagnostic bundle builder test"() {

        when: "bundle configured with builder"
        DiagnosticBundle bundle = DiagnosticBundle.builder().printStartupStats(true).build()

        then: "configured"
        bundle.statsConfig == true
        bundle.config == null
        bundle.treeConfig == null

        when: "all options configured"
        bundle = DiagnosticBundle.builder()
                .printStartupStats(false)
                .printConfiguration(new DiagnosticConfig().printAll())
                .printContextTree(new ContextTreeConfig())
                .build()

        then: "configured"
        bundle.statsConfig == false
        bundle.config.isPrintBundles()
        bundle.treeConfig.hiddenItems.empty

        when: "no options configured"
        bundle = DiagnosticBundle.builder().build()

        then: "nothing enabled"
        bundle.statsConfig == null
        bundle.config == null
        bundle.treeConfig == null
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(
                    GuiceBundle.builder()
                            .enableAutoConfig(FooResource.package.name)
                            .searchCommands()
                            .bundles(new Foo2Bundle())
                            .modules(new FooModule())
                            // intentional duplicate to increment REG
                            .extensions(FooBundleResource, FooBundleResource)
                            .disableInstallers(LifeCycleInstaller)
                            .strictScopeControl()
                            .printDiagnosticInfo()
                            .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}