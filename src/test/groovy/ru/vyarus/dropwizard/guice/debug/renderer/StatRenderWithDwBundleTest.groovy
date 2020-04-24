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

    GUICEY started in 624.6 ms (313.5 ms config / 311.0 ms run / 0 jersey)
    │
    ├── [0.48%] CLASSPATH scanned in 3.158 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │
    ├── [43%] BUNDLES processed in 270.0 ms
    │   ├── 2 resolved in 11.67 ms
    │   ├── 6 initialized in 17.82 ms
    │   └── 1 dropwizard bundles initialized in 240.1 ms
    │
    ├── [1.9%] COMMANDS processed in 12.73 ms
    │   └── registered 2 commands
    │
    ├── [5.1%] MODULES processed in 32.14 ms
    │   ├── 5 modules autowired
    │   ├── 7 elements found in 4 user modules in 27.39 ms
    │   └── 0 extensions detected from 2 acceptable bindings
    │
    ├── [4.6%] INSTALLERS processed in 29.92 ms
    │   ├── registered 12 installers
    │   └── 3 extensions recognized from 9 classes in 9.009 ms
    │
    ├── [39%] INJECTOR created in 243.3 ms
    │   ├── Module execution: 161 ms
    │   ├── Interceptors creation: 1 ms
    │   ├── TypeListeners & ProvisionListener creation: 2 ms
    │   ├── Scopes creation: 1 ms
    │   ├── Binding creation: 22 ms
    │   ├── Binding initialization: 27 ms
    │   ├── Collecting injection requests: 2 ms
    │   ├── Static validation: 3 ms
    │   ├── Instance member validation: 3 ms
    │   ├── Provider verification: 1 ms
    │   ├── Static member injection: 10 ms
    │   ├── Instance injection: 3 ms
    │   └── Preloading singletons: 5 ms
    │
    ├── [0.64%] EXTENSIONS installed in 4.007 ms
    │   ├── 3 extensions installed
    │   └── declared as: 2 manual, 1 scan, 0 binding
    │
    └── [5.1%] remaining 32 ms

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
