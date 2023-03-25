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
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 31.07.2016
 */
@TestGuiceyApp(App)
class StatRendererTest extends BaseDiagnosticTest {

    @Inject
    GuiceyConfigurationInfo info
    StatsRenderer renderer

    void setup() {
        renderer = new StatsRenderer(info)
    }

    def "Check guicey app stats render"() {
        /* render would look like:

    GUICEY started in 441.1 ms (104.8 ms config / 336.2 ms run / 0 jersey)
    │
    ├── [0.91%] CLASSPATH scanned in 4.245 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │
    ├── [10%] BUNDLES processed in 46.23 ms
    │   ├── 2 resolved in 19.40 ms
    │   └── 6 initialized in 26.31 ms
    │
    ├── [3.6%] COMMANDS processed in 16.81 ms
    │   └── registered 2 commands
    │
    ├── [7.7%] MODULES processed in 34.95 ms
    │   ├── 5 modules autowired
    │   ├── 7 elements found in 4 user modules in 30.22 ms
    │   └── 0 extensions detected from 2 acceptable bindings
    │
    ├── [9.1%] INSTALLERS processed in 40.61 ms
    │   ├── registered 12 installers
    │   └── 3 extensions recognized from 9 classes in 13.05 ms
    │
    ├── [54%] INJECTOR created in 236.6 ms
    │   ├── Module execution: 143 ms
    │   ├── Interceptors creation: 1 ms
    │   ├── TypeListeners & ProvisionListener creation: 2 ms
    │   ├── Scopes creation: 1 ms
    │   ├── Binding creation: 22 ms
    │   ├── Private environment creation: 1 ms
    │   ├── Binding initialization: 35 ms
    │   ├── Binding indexing: 1 ms
    │   ├── Collecting injection requests: 2 ms
    │   ├── Static validation: 4 ms
    │   ├── Instance member validation: 3 ms
    │   ├── Provider verification: 1 ms
    │   ├── Static member injection: 10 ms
    │   ├── Instance injection: 2 ms
    │   └── Preloading singletons: 6 ms
    │
    ├── [0.91%] EXTENSIONS installed in 4.549 ms
    │   ├── 3 extensions installed
    │   └── declared as: 2 manual, 1 scan, 0 binding
    │
    └── [14%] remaining 61 ms

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