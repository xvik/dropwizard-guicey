package ru.vyarus.dropwizard.guice.config.debug.renderer

import io.dropwizard.Application
import io.dropwizard.Configuration
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
 * @since 31.07.2016
 */
@UseGuiceyApp(App)
class StatRendererTest extends BaseDiagnosticTest {

    @Inject
    StatsRenderer renderer

    def "Check guicey app stats render"() {
        /* render would look like:

    GUICEY started in 315.2 ms (74.22 ms config / 239.2 ms run / 1.740 ms jersey)
    │
    ├── [0.63%] CLASSPATH scanned in 2.831 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │
    ├── [8.9%] BUNDLES processed in 28.19 ms
    │   ├── 2 resolved in 12.26 ms
    │   └── 7 bundles initialized in 16.59 ms
    │
    ├── [3.2%] COMMANDS processed in 10.59 ms
    │   └── registered 2 commands
    │
    ├── [10%] INSTALLERS executed in 32.30 ms
    │   ├── registered 12 installers
    │   └── 3 extensions recognized from 7 classes in 8.278 ms
    │
    ├── [66%] INJECTOR created in 207.2 ms
    │   └── from 6 guice modules
    │
    ├── [0.95%] EXTENSIONS installed in 3.518 ms
    │   ├── 2 by type
    │   └── 1 by instance
    │
    ├── [0.32%] JERSEY bridged in 1.740 ms
    │   ├── using 2 jersey installers
    │   └── 2 jersey extensions installed in 657.6 μs
    │
    └── [10%] remaining 32 ms

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
        render.contains("7 bundles initialized in")

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
}