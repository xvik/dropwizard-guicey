package ru.vyarus.dropwizard.guice.debug.renderer

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.debug.report.stat.StatsRenderer
import ru.vyarus.dropwizard.guice.diagnostic.BaseDiagnosticTest
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.LifeCycleInstaller
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 31.07.2016
 */
@UseDropwizardApp(App)
class StatRendererFullTest extends BaseDiagnosticTest {

    @Inject
    GuiceyConfigurationInfo info
    StatsRenderer renderer

    void setup() {
        renderer = new StatsRenderer(info)
    }

    def "Check dropwizard app stats render"() {
        /* render would look like:

    GUICEY started in 347.8 ms (68.39 ms config / 277.7 ms run / 1.642 ms jersey)
    │
    ├── [0.58%] CLASSPATH scanned in 2.946 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │
    ├── [8.6%] BUNDLES processed in 30.95 ms
    │   ├── 2 resolved in 12.57 ms
    │   └── 6 initialized in 18.01 ms
    │
    ├── [2.9%] COMMANDS processed in 10.63 ms
    │   └── registered 2 commands
    │
    ├── [8.1%] MODULES processed in 28.73 ms
    │   ├── 5 modules autowired
    │   ├── 2 bindings found in 4 user modules in 25.40 ms
    │   └── 0 extensions detected from 2 acceptable bindings
    │
    ├── [7.5%] INSTALLERS processed in 26.43 ms
    │   ├── registered 12 installers
    │   └── 3 extensions recognized from 9 classes in 9.191 ms
    │
    ├── [60%] INJECTOR created in 209.6 ms
    │   ├── Module execution: 125 ms
    │   ├── Interceptors creation: 1 ms
    │   ├── TypeListeners & ProvisionListener creation: 2 ms
    │   ├── Scopes creation: 1 ms
    │   ├── Binding creation: 22 ms
    │   ├── Binding initialization: 32 ms
    │   ├── Collecting injection requests: 2 ms
    │   ├── Static validation: 3 ms
    │   ├── Instance member validation: 4 ms
    │   ├── Static member injection: 8 ms
    │   ├── Instance injection: 3 ms
    │   └── Preloading singletons: 4 ms
    │
    ├── [0.86%] EXTENSIONS installed in 3.725 ms
    │   ├── using 3 enabled extensions
    │   └── from 2 manual, 1 classpath and 0 binding extension declarations
    │
    ├── [0.29%] JERSEY bridged in 1.642 ms
    │   ├── using 2 jersey installers
    │   └── 2 jersey extensions installed in 502.8 μs
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
        render.contains("2 bindings found in 4 user modules in")
        render.contains("0 extensions detected from 2 acceptable bindings")

        render.contains("] INSTALLERS")
        render.contains("registered 12 installers")
        render.contains("3 extensions recognized from 9 classes in")

        render.contains("] INJECTOR")
        render.contains("Module execution")

        render.contains("] EXTENSIONS")
        render.contains("using 3 enabled extensions")
        render.contains("from 2 manual, 1 classpath and 0 binding extension declarations")

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