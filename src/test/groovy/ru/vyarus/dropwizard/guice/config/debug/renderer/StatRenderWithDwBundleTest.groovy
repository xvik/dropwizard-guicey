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
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
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
    GuiceyConfigurationInfo info
    StatsRenderer renderer

    void setup() {
        renderer = new StatsRenderer(info)
    }

    def "Check guicey app stats render"() {
        /* render would look like:

    GUICEY started in 630.2 ms (328.4 ms config / 301.8 ms run / 0 jersey)
    │
    ├── [0.48%] CLASSPATH scanned in 3.116 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │
    ├── [45%] BUNDLES processed in 284.8 ms
    │   ├── 2 resolved in 12.81 ms
    │   ├── 7 initialized in 17.19 ms
    │   └── 1 dropwizard bundles initialized in 253.8 ms
    │
    ├── [1.7%] COMMANDS processed in 11.89 ms
    │   └── registered 2 commands
    │
    ├── [4.4%] INSTALLERS executed in 28.80 ms
    │   ├── registered 12 installers
    │   └── 3 extensions recognized from 7 classes in 6.967 ms
    │
    ├── [42%] INJECTOR created in 263.6 ms
    │   ├── from 5 guice modules
    │   │
    │   └── injector log
    │       ├── Module execution: 178 ms
    │       ├── Interceptors creation: 1 ms
    │       ├── TypeListeners & ProvisionListener creation: 2 ms
    │       ├── Scopes creation: 1 ms
    │       ├── Module annotated method scanners creation: 1 ms
    │       ├── Binding initialization: 29 ms
    │       ├── Collecting injection requests: 2 ms
    │       ├── Static validation: 4 ms
    │       ├── Instance member validation: 4 ms
    │       ├── Provider verification: 1 ms
    │       ├── Static member injection: 9 ms
    │       ├── Instance injection: 3 ms
    │       └── Preloading singletons: 5 ms
    │
    ├── [0.48%] EXTENSIONS installed in 3.837 ms
    │   ├── 2 by type
    │   └── 1 by instance
    │
    └── [6.0%] remaining 38 ms

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

        render.contains("] INSTALLERS")
        render.contains("registered 12 installers")
        render.contains("3 extensions recognized from 7 classes in")

        render.contains("] INJECTOR")
        render.contains("from 5 guice modules")
        render.contains("injector log")
        render.contains("Module execution")

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
