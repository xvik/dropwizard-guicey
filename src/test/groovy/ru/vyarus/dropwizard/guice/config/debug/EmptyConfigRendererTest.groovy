package ru.vyarus.dropwizard.guice.config.debug

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.VoidBundleLookup
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticConfig
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticRenderer
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject


/**
 * @author Vyacheslav Rusakov
 * @since 16.07.2016
 */
@UseGuiceyApp(App)
class EmptyConfigRendererTest extends Specification {

    @Inject
    DiagnosticRenderer renderer

    def "Check empty bundles"() {

        expect:
        renderer.renderReport(new DiagnosticConfig().printBundles()) == ""
    }

    def "Check empty installers"() {

        expect:
        renderer.renderReport(new DiagnosticConfig().printInstallers()) == ""
    }

    def "Check empty extensions"() {

        expect:
        renderer.renderReport(new DiagnosticConfig().printExtensions()) == ""
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().bundleLookup(new VoidBundleLookup()).build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}