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

    GUICEY started in 337.6 ms
    │
    ├── [0.59%] CLASSPATH scanned in 2.982 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │
    ├── [9.2%] BUNDLES processed in 31.18 ms
    │   ├── 2 resolved in 15.00 ms
    │   └── 6 processed
    │
    ├── [3.3%] COMMANDS processed in 11.87 ms
    │   └── registered 2 commands
    │
    ├── [4.5%] INSTALLERS initialized in 15.17 ms
    │   └── registered 9 installers
    │
    ├── [1.5%] EXTENSIONS initialized in 5.029 ms
    │   └── from 7 classes
    │
    ├── [69%] INJECTOR created in 233.9 ms
    │   ├── from 6 guice modules
    │   └── 3 extensions installed in 3.571 ms
    │
    ├── [0.30%] JERSEY bridged in 1.397 ms
    │   ├── using 2 jersey installers
    │   └── 2 jersey extensions installed in 579.3 μs
    │
    └── [12%] remaining 39 ms

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
        render.contains("6 processed")

        render.contains("] COMMANDS")
        render.contains("registered 2 commands")

        render.contains("] INSTALLERS")
        render.contains("registered 9 installers")

        render.contains("] EXTENSIONS")
        render.contains("from 7 classes")

        render.contains("] INJECTOR")
        render.contains("from 6 guice modules")
        render.contains("3 extensions installed in")

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
                            .bundles(
                            new FooBundle())
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