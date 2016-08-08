package ru.vyarus.dropwizard.guice.config.debug.renderer

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
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
class StatRendererTest extends AbstractTest {

    @Inject
    StatsRenderer renderer

    def "Check guicey app stats render"() {
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
        render.contains("using 9 installers")
        render.contains("from 7 classes")
        render.contains("3 extensions installed in")

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
                            .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}