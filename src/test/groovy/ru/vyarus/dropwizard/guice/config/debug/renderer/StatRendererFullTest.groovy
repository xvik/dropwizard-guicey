package ru.vyarus.dropwizard.guice.config.debug.renderer

import ru.vyarus.dropwizard.guice.diagnostic.BaseDiagnosticTest
import ru.vyarus.dropwizard.guice.module.context.debug.report.stat.StatsRenderer
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 31.07.2016
 */
@UseDropwizardApp(StatRendererTest.App)
class StatRendererFullTest extends BaseDiagnosticTest {

    @Inject
    StatsRenderer renderer

    def "Check dropwizard app stats render"() {
        /* render would look like:

    GUICEY started in 352.8 ms (82.81 ms config / 268.8 ms run / 1.185 ms jersey)
    │
    ├── [1.1%] CLASSPATH scanned in 4.446 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │
    ├── [11%] BUNDLES processed in 38.65 ms
    │   ├── 2 resolved in 18.13 ms
    │   └── 7 initialized in 19.53 ms
    │
    ├── [2.8%] COMMANDS processed in 10.92 ms
    │   └── registered 2 commands
    │
    ├── [8.0%] INSTALLERS executed in 28.03 ms
    │   ├── registered 12 installers
    │   └── 3 extensions recognized from 7 classes in 7.647 ms
    │
    ├── [66%] INJECTOR created in 234.9 ms
    │   ├── from 6 guice modules
    │   │
    │   └── injector log
    │       ├── Module execution: 141 ms
    │       ├── Interceptors creation: 1 ms
    │       ├── TypeListeners & ProvisionListener creation: 2 ms
    │       ├── Scopes creation: 1 ms
    │       ├── Binding creation: 23 ms
    │       ├── Binding initialization: 36 ms
    │       ├── Binding indexing: 1 ms
    │       ├── Collecting injection requests: 2 ms
    │       ├── Static validation: 4 ms
    │       ├── Instance member validation: 4 ms
    │       ├── Provider verification: 1 ms
    │       ├── Static member injection: 8 ms
    │       ├── Instance injection: 2 ms
    │       └── Preloading singletons: 5 ms
    │
    ├── [0.85%] EXTENSIONS installed in 3.956 ms
    │   ├── 2 by type
    │   └── 1 by instance
    │
    ├── [0.28%] JERSEY bridged in 1.185 ms
    │   ├── using 2 jersey installers
    │   └── 2 jersey extensions installed in 475.7 μs
    │
    └── [9.7%] remaining 34 ms

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

        render.contains("] COMMANDS")
        render.contains("registered 2 commands")

        render.contains("] INSTALLERS")
        render.contains("registered 12 installers")
        render.contains("3 extensions recognized from 7 classes in")

        render.contains("] INJECTOR")
        render.contains("from 6 guice modules")
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

}