package ru.vyarus.dropwizard.guice.debug

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.VoidBundleLookup
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticConfig
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticRenderer
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 16.07.2016
 */
@TestGuiceyApp(App)
class EmptyConfigRendererTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info
    DiagnosticRenderer renderer

    void setup() {
        renderer = new DiagnosticRenderer(info)
    }

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
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundleLookup(new VoidBundleLookup())
                    .noDefaultInstallers()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}