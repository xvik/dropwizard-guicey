package ru.vyarus.dropwizard.guice.config.debug.renderer

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.context.debug.report.stat.StatsRenderer
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 31.07.2016
 */
@UseDropwizardApp(StatRendererTest.App)
class StatRendererFullTest extends AbstractTest {

    @Inject
    StatsRenderer renderer

    def "Check dropwizard app stats render"() {
        /* render would look like:

     GUICEY started in 425.7 ms
    │
    ├── [0,94%] CLASSPATH scanned in 4.201 ms
    │   ├── scanned 5 classes
    │   └── recognized 4 classes (80% of scanned)
    │
    ├── [2,8%] COMMANDS processed in 12.65 ms
    │   └── registered 2 commands
    │
    ├── [6,8%] BUNDLES processed in 29.55 ms
    │   ├── 2 resolved in 7.599 ms
    │   └── 5 processed
    │
    ├── [88%] INJECTOR created in 375.8 ms
    │   ├── installers prepared in 11.49 ms
    │   │
    │   ├── extensions recognized in 9.287 ms
    │   │   ├── using 11 installers
    │   │   └── from 7 classes
    │   │
    │   └── 3 extensions installed in 4.170 ms
    │
    ├── [1,8%] HK bridged in 7.321 ms
    │   ├── using 2 jersey installers
    │   └── 2 jersey extensions installed in 714.9 μs
    │
    └── [1,2%] remaining 5 ms

         */

        setup:
        String render = render()

        expect: "main sections rendered"
        render.contains("GUICEY started in")

        render.contains("] CLASSPATH")
        render.contains("scanned 5 classes")
        render.contains("recognized 4 classes (80% of scanned)")

        render.contains("] COMMANDS")
        render.contains("registered 2 commands")


        render.contains("] BUNDLES")
        render.contains("2 resolved in")
        render.contains("5 processed")


        render.contains("] INJECTOR")
        render.contains("installers prepared in")
        render.contains("extensions recognized in")
        render.contains("using 11 installers")
        render.contains("from 7 classes")
        render.contains("3 extensions installed in")

        render.contains("] HK bridged in ")
        render.contains("using 2 jersey installers")
        render.contains("2 jersey extensions installed in")

        render.contains("] remaining")
    }

    String render() {
        renderer.renderReport(false).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }

}