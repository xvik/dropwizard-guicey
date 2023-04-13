package ru.vyarus.dropwizard.guice.debug.renderer

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.debug.report.stat.StatsRenderer
import ru.vyarus.dropwizard.guice.diagnostic.BaseDiagnosticTest
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 31.07.2016
 */
@TestDropwizardApp(App)
class StatRendererFullTest extends BaseDiagnosticTest {

    @Inject
    GuiceyConfigurationInfo info
    StatsRenderer renderer

    void setup() {
        renderer = new StatsRenderer(info)
    }

    def "Check dropwizard app stats render"() {
        /* render would look like:

    GUICEY started in 351.4 ms (74.63 ms config / 275.6 ms run / 1.176 ms jersey)
    │
    ├── [0.85%] CLASSPATH scanned in 3.649 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │
    ├── [8.8%] BUNDLES processed in 31.36 ms
    │   ├── 2 resolved in 14.52 ms
    │   └── 6 initialized in 16.42 ms
    │
    ├── [3.4%] COMMANDS processed in 12.48 ms
    │   └── registered 2 commands
    │
    ├── [11%] MODULES processed in 37.23 ms
    │   ├── 5 modules autowired
    │   ├── 7 elements found in 4 user modules in 33.00 ms
    │   └── 0 extensions detected from 2 acceptable bindings
    │
    ├── [8.3%] INSTALLERS processed in 29.78 ms
    │   ├── registered 12 installers
    │   └── 3 extensions recognized from 9 classes in 8.884 ms
    │
    ├── [56%] INJECTOR created in 197.5 ms
    │   ├── Module execution: 117 ms
    │   ├── Interceptors creation: 1 ms
    │   ├── TypeListeners & ProvisionListener creation: 2 ms
    │   ├── Scopes creation: 1 ms
    │   ├── Binding creation: 22 ms
    │   ├── Binding initialization: 27 ms
    │   ├── Binding indexing: 1 ms
    │   ├── Collecting injection requests: 1 ms
    │   ├── Static validation: 3 ms
    │   ├── Instance member validation: 4 ms
    │   ├── Static member injection: 8 ms
    │   ├── Instance injection: 2 ms
    │   └── Preloading singletons: 5 ms
    │
    ├── [0.85%] EXTENSIONS installed in 3.616 ms
    │   ├── 3 extensions installed
    │   └── declared as: 2 manual, 1 scan, 0 binding
    │
    ├── [0.28%] JERSEY bridged in 1.176 ms
    │   ├── using 2 jersey installers
    │   └── 2 jersey extensions installed in 493.4 μs
    │
    └── [11%] remaining 38 ms

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
        render.contains("6 initialized in")

        render.contains("] COMMANDS")
        render.contains("registered 2 commands")

        render.contains("] MODULES")
        render.contains("5 modules autowired")
        render.contains("7 elements found in 4 user modules in")
        render.contains("0 extensions detected from 2 acceptable bindings")

        render.contains("] INSTALLERS")
        render.contains("registered 12 installers")
        render.contains("3 extensions recognized from 9 classes in")

        render.contains("] INJECTOR")
        render.contains("Module execution")

        render.contains("] EXTENSIONS")
        render.contains("3 extensions installed")
        render.contains("declared as: 2 manual, 1 scan, 0 binding")

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
                            .modules(new FooModule())
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