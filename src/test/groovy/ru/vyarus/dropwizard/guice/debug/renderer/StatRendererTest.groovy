package ru.vyarus.dropwizard.guice.debug.renderer

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.diagnostic.BaseDiagnosticTest
import ru.vyarus.dropwizard.guice.diagnostic.support.bundle.FooBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooModule
import ru.vyarus.dropwizard.guice.diagnostic.support.features.FooResource
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.debug.report.stat.StatsRenderer
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
    GuiceyConfigurationInfo info
    StatsRenderer renderer

    void setup() {
        renderer = new StatsRenderer(info)
    }

    def "Check guicey app stats render"() {
        /* render would look like:

    GUICEY started in 328.0 ms (84.14 ms config / 243.8 ms run / 0 jersey)
    │
    ├── [1.2%] CLASSPATH scanned in 4.656 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │
    ├── [11%] BUNDLES processed in 35.12 ms
    │   ├── 2 resolved in 17.16 ms
    │   └── 6 initialized in 17.49 ms
    │
    ├── [4.6%] COMMANDS processed in 15.94 ms
    │   └── registered 2 commands
    │
    ├── [9.2%] MODULES processed in 30.42 ms
    │   ├── 5 modules autowired
    │   ├── 2 bindings found in 4 user modules in 26.18 ms
    │   └── 0 extensions detected from 2 acceptable bindings
    │
    ├── [9.2%] INSTALLERS processed in 30.49 ms
    │   ├── registered 12 installers
    │   └── 3 extensions recognized from 9 classes in 9.072 ms
    │
    ├── [54%] INJECTOR created in 176.9 ms
    │   ├── Module execution: 105 ms
    │   ├── Interceptors creation: 1 ms
    │   ├── TypeListeners & ProvisionListener creation: 1 ms
    │   ├── Scopes creation: 1 ms
    │   ├── Binding creation: 18 ms
    │   ├── Binding initialization: 26 ms
    │   ├── Collecting injection requests: 1 ms
    │   ├── Static validation: 3 ms
    │   ├── Instance member validation: 3 ms
    │   ├── Provider verification: 1 ms
    │   ├── Static member injection: 7 ms
    │   ├── Instance injection: 3 ms
    │   └── Preloading singletons: 4 ms
    │
    ├── [0.92%] EXTENSIONS installed in 3.262 ms
    │   ├── using 3 enabled extensions
    │   └── from 2 manual, 1 classpath and 0 binding extension declarations
    │
    └── [10%] remaining 34 ms

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