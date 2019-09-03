package ru.vyarus.dropwizard.guice.debug.renderer

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
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.debug.report.stat.StatsRenderer
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
    GuiceyConfigurationInfo info
    StatsRenderer renderer

    void setup() {
        renderer = new StatsRenderer(info)
    }

    def "Check guicey app stats render"() {
        /* render would look like:

    GUICEY started in 672.1 ms (390.1 ms config / 282.0 ms run / 0 jersey)
    │
    ├── [0.60%] CLASSPATH scanned in 4.377 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │
    ├── [51%] BUNDLES processed in 345.1 ms
    │   ├── 2 resolved in 16.80 ms
    │   ├── 6 initialized in 23.29 ms
    │   └── 1 dropwizard bundles initialized in 304.7 ms
    │
    ├── [2.1%] COMMANDS processed in 14.72 ms
    │   └── registered 2 commands
    │
    ├── [3.9%] MODULES processed in 26.17 ms
    │   ├── 5 modules autowired
    │   ├── 2 bindings found in 4 user modules in 23.23 ms
    │   └── 0 extensions detected from 2 acceptable bindings
    │
    ├── [4.0%] INSTALLERS processed in 27.22 ms
    │   ├── registered 12 installers
    │   └── 3 extensions recognized from 9 classes in 8.074 ms
    │
    ├── [33%] INJECTOR created in 224.0 ms
    │   ├── Module execution: 155 ms
    │   ├── Interceptors creation: 1 ms
    │   ├── TypeListeners & ProvisionListener creation: 2 ms
    │   ├── Scopes creation: 1 ms
    │   ├── Binding creation: 17 ms
    │   ├── Private environment creation: 1 ms
    │   ├── Binding initialization: 24 ms
    │   ├── Collecting injection requests: 1 ms
    │   ├── Static validation: 3 ms
    │   ├── Instance member validation: 3 ms
    │   ├── Static member injection: 8 ms
    │   ├── Instance injection: 2 ms
    │   └── Preloading singletons: 4 ms
    │
    ├── [0.45%] EXTENSIONS installed in 3.300 ms
    │   ├── 3 extensions installed
    │   └── declared as: 2 manual, 1 scan, 0 binding
    │
    └── [4.3%] remaining 29 ms

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
        render.contains("1 dropwizard bundles initialized in")

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
                            .dropwizardBundles(new DBundle())
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

    static class DBundle implements ConfiguredBundle {}
}
