package ru.vyarus.dropwizard.guice.debug

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticConfig
import ru.vyarus.dropwizard.guice.debug.report.option.OptionsConfig
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeConfig
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.Foo2Bundle
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundleResource
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.LookupBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.dwbundle.FooDwBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.diagnostic.support.module.ModuleWithExtensions
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 12.07.2016
 */
@TestDropwizardApp(App)
// important to track HK2 part also
class DiagnosticBundleTest extends AbstractTest {

    def "Check logging"() {

        // test checks just no exceptions
        // used for real reporting manual testing

        expect: "correct startup"
        true

    }

    def "Check empty config detection"() {

        when: "empty config provided"
        ConfigurationDiagnostic.builder()
                .printConfiguration(new DiagnosticConfig())

        then: "error"
        thrown(IllegalStateException)

    }

    def "Diagnostic bundle builder test"() {

        when: "bundle configured with builder"
        ConfigurationDiagnostic bundle = ConfigurationDiagnostic.builder().printStartupStats(true).build()

        then: "configured"
        bundle.statsConfig == true
        bundle.optionsConfig == null
        bundle.config == null
        bundle.treeConfig == null

        when: "all options configured"
        bundle = ConfigurationDiagnostic.builder()
                .printStartupStats(false)
                .printOptions(new OptionsConfig().showNotUsedMarker().showNotDefinedOptions())
                .printConfiguration(new DiagnosticConfig().printAll())
                .printContextTree(new ContextTreeConfig())
                .build()

        then: "configured"
        bundle.statsConfig == false
        bundle.optionsConfig.isShowNotUsedMarker()
        bundle.config.isPrintBundles()
        bundle.treeConfig.hiddenItems.empty

        when: "no options configured"
        bundle = ConfigurationDiagnostic.builder().build()

        then: "nothing enabled"
        bundle.statsConfig == null
        bundle.optionsConfig == null
        bundle.config == null
        bundle.treeConfig == null
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            // just to show lookup in report
            PropertyBundleLookup.enableBundles(LookupBundle)

            bootstrap.addBundle(
                    GuiceBundle.builder()
                            .enableAutoConfig(FooResource.package.name)
                            .searchCommands()
                            .bundles(new Foo2Bundle())
                            .modules(new FooModule(), new ModuleWithExtensions())
                            .dropwizardBundles(new FooDwBundle())
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