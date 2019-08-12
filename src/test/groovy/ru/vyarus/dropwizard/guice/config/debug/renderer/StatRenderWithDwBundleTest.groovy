package ru.vyarus.dropwizard.guice.config.debug.renderer

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.ConfiguredBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.diagnostic.BaseDiagnosticTest
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.context.debug.DiagnosticBundle
import ru.vyarus.dropwizard.guice.module.context.debug.report.stat.StatsRenderer
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2019
 */
@UseGuiceyApp(App)
class StatRenderWithDwBundleTest extends BaseDiagnosticTest {

    @Inject
    StatsRenderer renderer

    def "Check guicey app stats render"() {
        /* render would look like:

    GUICEY started in 628.8 ms (320.9 ms config / 307.9 ms run / 0 jersey)
    │
    ├── [0.64%] CLASSPATH scanned in 4.304 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │
    ├── [44%] BUNDLES processed in 278.4 ms
    │   ├── 2 resolved in 12.72 ms
    │   ├── 7 initialized in 205.7 μs
    │   └── 1 dropwizard bundles initialized in 19.79 ms
    │
    ├── [1.8%] COMMANDS processed in 11.48 ms
    │   └── registered 2 commands
    │
    ├── [4.1%] INSTALLERS executed in 26.35 ms
    │   ├── registered 12 installers
    │   └── 3 extensions recognized from 7 classes in 6.614 ms
    │
    ├── [44%] INJECTOR created in 276.0 ms
    │   └── from 6 guice modules
    │
    ├── [0.48%] EXTENSIONS installed in 3.762 ms
    │   ├── 2 by type
    │   └── 1 by instance
    │
    ├── [0.0%] JERSEY bridged in 0
    │   ├── using 2 jersey installers
    │   └── 2 jersey extensions installed in 0
    │
    └── [4.9%] remaining 31 ms

         */

        setup:
        String render = render()

        expect: "main sections rendered"
        render.contains("GUICEY started in")

        render.contains("] CLASSPATH")
        render.contains("scanned 5 classes")
        render.contains("recognized 4 classes (80% of scanned)")

        render.contains("] BUNDLES")
        render.contains("2 resolved in")
        render.contains("7 initialized in")
        render.contains("1 dropwizard bundles initialized in")

        render.contains("] COMMANDS")
        render.contains("registered 2 commands")

        render.contains("] INSTALLERS")
        render.contains("registered 12 installers")
        render.contains("3 extensions recognized from 7 classes in")

        render.contains("] INJECTOR")
        render.contains("from 6 guice modules")

        render.contains("] EXTENSIONS")
        render.contains("2 by type")
        render.contains("1 by instance")

        render.contains("] JERSEY bridged in ")
        render.contains("using 2 jersey installers")
        render.contains("2 jersey extensions installed in")

        render.contains("] remaining")
    }

    String render() {
        renderer.renderReport(false).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(
                    GuiceBundle.builder()
                            .enableAutoConfig(FooResource.package.name)
                            .searchCommands()
                            .dropwizardBundles(new DBundle())
                            .bundles(new FooBundle())
                            .modules(new FooModule(), new DiagnosticBundle.DiagnosticModule())
                            .disableInstallers(LifeCycleInstaller)
                            .strictScopeControl()
                            .printDiagnosticInfo()
                            .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class DBundle implements ConfiguredBundle {}
}
