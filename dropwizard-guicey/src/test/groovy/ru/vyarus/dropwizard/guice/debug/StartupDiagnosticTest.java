package ru.vyarus.dropwizard.guice.debug;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.lifecycle.Managed;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.debug.report.start.DropwizardBundlesTracker;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordLogs;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordedLogs;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;

/**
 * @author Vyacheslav Rusakov
 * @since 11.03.2025
 */
public class StartupDiagnosticTest extends AbstractPlatformTest {

    @Test
    void testGuiceyRunReport() {
        String out = run(Test1.class);
        Assertions.assertThat(out).contains("Application startup time: \n" +
                "\n" +
                "\tJVM time before                    : 111 ms\n" +
                "\n" +
                "\tApplication startup                : 111 ms\n" +
                "\t\tDropwizard initialization          : 111 ms\n" +
                "\t\t\tGuiceBundle                        : 111 ms (finished since start at 111 ms)\n" +
                "\t\t\t\tBundle builder time                : 111 ms\n" +
                "\t\t\t\tHooks processing                   : 111 ms\n" +
                "\t\t\t\t\tStartupDiagnosticTest$Test1$$Lambda$111/1111111: 111 ms\n" +
                "\t\t\t\tClasspath scan                     : 111 ms\n" +
                "\t\t\t\tCommands processing                : 111 ms\n" +
                "\t\t\t\t\tNonInjactableCommand               : 111 ms\n" +
                "\t\t\t\t\tNonInjactableCommand               : 111 ms\n" +
                "\t\t\t\tBundles lookup                     : 111 ms\n" +
                "\t\t\t\tGuicey bundles init                : 111 ms\n" +
                "\t\t\t\t\tCoreInstallersBundle               : 111 ms\n" +
                "\t\t\t\t\tWebInstallersBundle                : 111 ms\n" +
                "\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\t\tInstallers resolution              : 111 ms\n" +
                "\t\t\t\t\tScanned extensions recognition     : 111 ms\n" +
                "\t\t\t\tListeners time                     : 111 ms\n" +
                "\t\t\t\t\tConfigurationHooksProcessedEvent   : 111 ms\n" +
                "\t\t\t\t\tBeforeInitEvent                    : 111 ms\n" +
                "\t\t\t\t\tBundlesResolvedEvent               : 111 ms\n" +
                "\t\t\t\t\tBundlesInitializedEvent            : 111 ms\n" +
                "\t\t\t\t\tCommandsResolvedEvent              : 111 ms\n" +
                "\t\t\t\t\tInstallersResolvedEvent            : 111 ms\n" +
                "\t\t\t\t\tClasspathExtensionsResolvedEvent   : 111 ms\n" +
                "\t\t\t\t\tInitializedEvent                   : 111 ms\n" +
                "\n" +
                "\t\tDropwizard run                     : 111 ms\n" +
                "\t\t\tConfiguration and Environment      : 111 ms\n" +
                "\t\t\tGuiceBundle                        : 111 ms\n" +
                "\t\t\t\tConfiguration analysis             : 111 ms\n" +
                "\t\t\t\tGuicey bundles run                 : 111 ms\n" +
                "\t\t\t\t\tCoreInstallersBundle               : 111 ms\n" +
                "\t\t\t\t\tWebInstallersBundle                : 111 ms\n" +
                "\t\t\t\tGuice modules processing           : 111 ms\n" +
                "\t\t\t\t\tBindings resolution                : 111 ms\n" +
                "\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\t\tExtensions registration            : 111 ms\n" +
                "\t\t\t\t\tGuice bindings analysis            : 111 ms\n" +
                "\t\t\t\t\tExtensions installation            : 111 ms\n" +
                "\t\t\t\tInjector creation                  : 111 ms\n");

        Assertions.assertThat(out).contains("\t\t\t\tListeners time                     : 111 ms\n" +
                "\t\t\t\t\tBeforeRunEvent                     : 111 ms\n" +
                "\t\t\t\t\tBundlesStartedEvent                : 111 ms\n" +
                "\t\t\t\t\tModulesAnalyzedEvent               : 111 ms\n" +
                "\t\t\t\t\tExtensionsResolvedEvent            : 111 ms\n" +
                "\t\t\t\t\tInjectorCreationEvent              : 111 ms\n" +
                "\t\t\t\t\tExtensionsInstalledByEvent         : 111 ms\n" +
                "\t\t\t\t\tExtensionsInstalledEvent           : 111 ms\n" +
                "\t\t\t\t\tApplicationRunEvent                : 111 ms\n" +
                "\n" +
                "\t\tWeb server startup                 : 111 ms\n" +
                "\t\t\tLifecycle simulation time          : 111 ms\n" +
                "\t\t\t\tmanaged   ExecutorServiceManager             : 111 ms\n" +
                "\t\t\t\tmanaged   RegistryShutdown                   : 111 ms\n" +
                "\t\t\t\tmanaged   DummyManaged                       : 111 ms\n" +
                "\t\t\t\tGuicey time                        : 111 ms\n" +
                "\t\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\t\tListeners time                     : 111 ms\n" +
                "\t\t\t\t\t\tApplicationStartedEvent            : 111 ms");

        Assertions.assertThat(out).contains("Application shutdown time: \n" +
                "\n" +
                "\tApplication shutdown               : 111 ms\n" +
                "\t\tmanaged   DummyManaged                       : 111 ms\n" +
                "\t\tmanaged   RegistryShutdown                   : 111 ms\n" +
                "\t\tmanaged   ExecutorServiceManager             : 111 ms\n" +
                "\t\tListeners time                     : 111 ms\n" +
                "\t\t\tApplicationShutdownEvent           : 111 ms\n" +
                "\t\t\tApplicationStoppedEvent            : 111 ms");
    }

    @Test
    void testDwRunReport() {
        String out = run(Test2.class);
        Assertions.assertThat(out).contains("Application startup time: \n" +
                "\n" +
                "\tJVM time before                    : 111 ms\n" +
                "\n" +
                "\tApplication startup                : 111 ms\n" +
                "\t\tDropwizard initialization          : 111 ms\n" +
                "\t\t\tGuiceBundle                        : 111 ms (finished since start at 111 ms)\n" +
                "\t\t\t\tBundle builder time                : 111 ms\n" +
                "\t\t\t\tHooks processing                   : 111 ms\n" +
                "\t\t\t\t\tStartupDiagnosticTest$Test2$$Lambda$111/1111111: 111 ms\n" +
                "\t\t\t\tClasspath scan                     : 111 ms\n" +
                "\t\t\t\tCommands processing                : 111 ms\n" +
                "\t\t\t\t\tNonInjactableCommand               : 111 ms\n" +
                "\t\t\t\t\tNonInjactableCommand               : 111 ms\n" +
                "\t\t\t\tBundles lookup                     : 111 ms\n" +
                "\t\t\t\tGuicey bundles init                : 111 ms\n" +
                "\t\t\t\t\tCoreInstallersBundle               : 111 ms\n" +
                "\t\t\t\t\tWebInstallersBundle                : 111 ms\n" +
                "\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\t\tInstallers resolution              : 111 ms\n" +
                "\t\t\t\t\tScanned extensions recognition     : 111 ms\n" +
                "\t\t\t\tListeners time                     : 111 ms\n" +
                "\t\t\t\t\tConfigurationHooksProcessedEvent   : 111 ms\n" +
                        "\t\t\t\t\tBeforeInitEvent                    : 111 ms\n" +
                        "\t\t\t\t\tBundlesResolvedEvent               : 111 ms\n" +
                        "\t\t\t\t\tBundlesInitializedEvent            : 111 ms\n" +
                        "\t\t\t\t\tCommandsResolvedEvent              : 111 ms\n" +
                        "\t\t\t\t\tInstallersResolvedEvent            : 111 ms\n" +
                        "\t\t\t\t\tClasspathExtensionsResolvedEvent   : 111 ms\n" +
                        "\t\t\t\t\tInitializedEvent                   : 111 ms\n" +
                "\n" +
                "\t\tDropwizard run                     : 111 ms\n" +
                "\t\t\tConfiguration and Environment      : 111 ms\n" +
                "\t\t\tGuiceBundle                        : 111 ms\n" +
                "\t\t\t\tConfiguration analysis             : 111 ms\n" +
                "\t\t\t\tGuicey bundles run                 : 111 ms\n" +
                "\t\t\t\t\tCoreInstallersBundle               : 111 ms\n" +
                "\t\t\t\t\tWebInstallersBundle                : 111 ms\n" +
                "\t\t\t\tGuice modules processing           : 111 ms\n" +
                "\t\t\t\t\tBindings resolution                : 111 ms\n" +
                "\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\t\tExtensions registration            : 111 ms\n" +
                "\t\t\t\t\tGuice bindings analysis            : 111 ms\n" +
                "\t\t\t\t\tExtensions installation            : 111 ms\n" +
                "\t\t\t\tInjector creation                  : 111 ms\n");

        Assertions.assertThat(out).contains("\t\t\t\tListeners time                     : 111 ms\n" +
                "\t\t\t\t\tBeforeRunEvent                     : 111 ms\n" +
                        "\t\t\t\t\tBundlesStartedEvent                : 111 ms\n" +
                        "\t\t\t\t\tModulesAnalyzedEvent               : 111 ms\n" +
                        "\t\t\t\t\tExtensionsResolvedEvent            : 111 ms\n" +
                        "\t\t\t\t\tInjectorCreationEvent              : 111 ms\n" +
                        "\t\t\t\t\tExtensionsInstalledByEvent         : 111 ms\n" +
                        "\t\t\t\t\tExtensionsInstalledEvent           : 111 ms\n" +
                        "\t\t\t\t\tApplicationRunEvent                : 111 ms\n" +
                "\n" +
                "\t\tWeb server startup                 : 111 ms\n" +
                "\t\t\tJetty lifecycle time               : 111 ms\n" +
                "\t\t\t\tmanaged   ExecutorServiceManager             : 111 ms\n" +
                "\t\t\t\tmanaged   RegistryShutdown                   : 111 ms\n" +
                "\t\t\t\tmanaged   DummyManaged                       : 111 ms\n" +
                "\t\t\t\tJersey time                        : 111 ms\n" +
                "\t\t\t\t\tGuicey time                        : 111 ms\n" +
                "\t\t\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\t\t\tListeners time                     : 111 ms\n" +
                "\t\t\t\t\t\t\tJerseyConfigurationEvent           : 111 ms\n" +
                "\t\t\t\t\t\t\tJerseyExtensionsInstalledByEvent   : 111 ms\n" +
                "\t\t\t\t\t\t\tJerseyExtensionsInstalledEvent     : 111 ms\n" +
                "\t\t\t\t\t\t\tApplicationStartedEvent            : 111 ms");

        Assertions.assertThat(out).contains("Application shutdown time: \n" +
                "\n" +
                "\tApplication shutdown               : 111 ms\n" +
                "\t\tmanaged   DummyManaged                       : 111 ms\n" +
                "\t\tmanaged   RegistryShutdown                   : 111 ms\n" +
                "\t\tmanaged   ExecutorServiceManager             : 111 ms\n" +
                "\t\tListeners time                     : 111 ms\n" +
                "\t\t\tApplicationShutdownEvent           : 111 ms\n" +
                "\t\t\tApplicationStoppedEvent            : 111 ms");
    }

    @Test
    void testRestStubsRunReport() {
        String out = run(Test3.class);
        Assertions.assertThat(out).contains("Application startup time: \n" +
                "\n" +
                "\tJVM time before                    : 111 ms\n" +
                "\n" +
                "\tApplication startup                : 111 ms\n" +
                "\t\tDropwizard initialization          : 111 ms\n" +
                "\t\t\tGuiceBundle                        : 111 ms (finished since start at 111 ms)\n" +
                "\t\t\t\tBundle builder time                : 111 ms\n" +
                "\t\t\t\tHooks processing                   : 111 ms\n" +
                "\t\t\t\t\tRestStubSupport                    : 111 ms\n" +
                "\t\t\t\t\tStartupDiagnosticTest$Test3$$Lambda$111/1111111: 111 ms\n" +
                "\t\t\t\tClasspath scan                     : 111 ms\n" +
                "\t\t\t\tCommands processing                : 111 ms\n" +
                "\t\t\t\t\tNonInjactableCommand               : 111 ms\n" +
                "\t\t\t\t\tNonInjactableCommand               : 111 ms\n" +
                "\t\t\t\tBundles lookup                     : 111 ms\n" +
                "\t\t\t\tGuicey bundles init                : 111 ms\n" +
                "\t\t\t\t\tCoreInstallersBundle               : 111 ms\n" +
                "\t\t\t\t\tWebInstallersBundle                : 111 ms\n" +
                "\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\t\tInstallers resolution              : 111 ms\n" +
                "\t\t\t\t\tScanned extensions recognition     : 111 ms\n" +
                "\t\t\t\tListeners time                     : 111 ms\n" +
                "\t\t\t\t\tConfigurationHooksProcessedEvent   : 111 ms\n" +
                        "\t\t\t\t\tBeforeInitEvent                    : 111 ms\n" +
                        "\t\t\t\t\tBundlesResolvedEvent               : 111 ms\n" +
                        "\t\t\t\t\tBundlesInitializedEvent            : 111 ms\n" +
                        "\t\t\t\t\tCommandsResolvedEvent              : 111 ms\n" +
                        "\t\t\t\t\tInstallersResolvedEvent            : 111 ms\n" +
                        "\t\t\t\t\tClasspathExtensionsResolvedEvent   : 111 ms\n" +
                        "\t\t\t\t\tInitializedEvent                   : 111 ms\n" +
                "\n" +
                "\t\tDropwizard run                     : 111 ms\n" +
                "\t\t\tConfiguration and Environment      : 111 ms\n" +
                "\t\t\tGuiceBundle                        : 111 ms\n" +
                "\t\t\t\tConfiguration analysis             : 111 ms\n" +
                "\t\t\t\tGuicey bundles run                 : 111 ms\n" +
                "\t\t\t\t\tCoreInstallersBundle               : 111 ms\n" +
                "\t\t\t\t\tWebInstallersBundle                : 111 ms\n" +
                "\t\t\t\tGuice modules processing           : 111 ms\n" +
                "\t\t\t\t\tBindings resolution                : 111 ms\n" +
                "\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\t\tExtensions registration            : 111 ms\n" +
                "\t\t\t\t\tGuice bindings analysis            : 111 ms\n" +
                "\t\t\t\t\tExtensions installation            : 111 ms\n" +
                "\t\t\t\tInjector creation                  : 111 ms\n");


        // NOTE: jersey events here because rest stub runs at rnu phase
        Assertions.assertThat(out).contains("\t\t\t\tListeners time                     : 111 ms\n" +
                "\t\t\t\t\tBeforeRunEvent                     : 111 ms\n" +
                        "\t\t\t\t\tBundlesStartedEvent                : 111 ms\n" +
                        "\t\t\t\t\tModulesAnalyzedEvent               : 111 ms\n" +
                        "\t\t\t\t\tExtensionsResolvedEvent            : 111 ms\n" +
                        "\t\t\t\t\tInjectorCreationEvent              : 111 ms\n" +
                        "\t\t\t\t\tExtensionsInstalledByEvent         : 111 ms\n" +
                        "\t\t\t\t\tExtensionsInstalledEvent           : 111 ms\n" +
                        "\t\t\t\t\tApplicationRunEvent                : 111 ms\n" +
                        "\t\t\t\t\tJerseyConfigurationEvent           : 111 ms\n" +
                        "\t\t\t\t\tJerseyExtensionsInstalledByEvent   : 111 ms\n" +
                        "\t\t\t\t\tJerseyExtensionsInstalledEvent     : 111 ms\n" +
                "\n" +
                "\t\tWeb server startup                 : 111 ms\n" +
                "\t\t\tLifecycle simulation time          : 111 ms\n" +
                "\t\t\t\tmanaged   ExecutorServiceManager             : 111 ms\n" +
                "\t\t\t\tmanaged   RegistryShutdown                   : 111 ms\n" +
                "\t\t\t\tmanaged   DummyManaged                       : 111 ms\n" +
                "\t\t\t\tGuicey time                        : 111 ms\n" +
                "\t\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\t\tListeners time                     : 111 ms\n" +
                "\t\t\t\t\t\tApplicationStartedEvent            : 111 ms");

        Assertions.assertThat(out).contains("Application shutdown time: \n" +
                "\n" +
                "\tApplication shutdown               : 111 ms\n" +
                "\t\tmanaged   DummyManaged                       : 111 ms\n" +
                "\t\tmanaged   RegistryShutdown                   : 111 ms\n" +
                "\t\tmanaged   ExecutorServiceManager             : 111 ms\n" +
                "\t\tListeners time                     : 111 ms\n" +
                "\t\t\tApplicationShutdownEvent           : 111 ms\n" +
                "\t\t\tApplicationStoppedEvent            : 111 ms");
    }

    @Test
    void testBundlesWarning() {
        String out = run(Test5.class);
        Assertions.assertThat(out).contains("Initialization time not tracked for bundles (move them after guice bundle to measure time): Bundle");

        Assertions.assertThat(out).contains("\tApplication startup                : 111 ms\n" +
                "\t\tDropwizard initialization          : 111 ms\n" +
                "\t\t\tBundle                             : finished since start at 111 ms\n" +
                "\t\t\tRecordedLogsTrackingBundle         : finished since start at 111 ms\n" +
                "\t\t\tGuiceBundle                        : 111 ms (finished since start at 111 ms)");

        Assertions.assertThat(out).contains("\t\tDropwizard run                     : 111 ms\n" +
                "\t\t\tConfiguration and Environment      : 111 ms\n" +
                "\t\t\tBundle                             : 111 ms\n" +
                "\t\t\tRecordedLogsTrackingBundle         : 111 ms\n" +
                "\t\t\tGuiceBundle                        : 111 ms");
    }

    @Test
    void testAnonymousBundle() {
        String out = run(Test6.class);

        Assertions.assertThat(out).contains("StartupDiagnosticTest$Test6$App$1  : finished since start at 111 ms");
    }

    @Test
    void testAnonymousManaged() {
        String out = run(Test7.class);

        Assertions.assertThat(out).contains("managed   StartupDiagnosticTest$Test7$1      : 111 ms");
    }

    @Disabled
    @TestGuiceyApp(AutoScanApplication.class)
    public static class Test1 {

        @EnableHook
        static GuiceyConfigurationHook hook = builder -> builder.printStartupTime();

        @Test
        void test() {
        }
    }

    @Disabled
    @TestDropwizardApp(AutoScanApplication.class)
    public static class Test2 {

        @EnableHook
        static GuiceyConfigurationHook hook = GuiceBundle.Builder::printStartupTime;

        @Test
        void test() {
        }
    }

    @Disabled
    @TestGuiceyApp(AutoScanApplication.class)
    public static class Test3 {

        @EnableHook
        static GuiceyConfigurationHook hook = GuiceBundle.Builder::printStartupTime;

        @StubRest
        RestClient rest;

        @Test
        void test() {
        }
    }

    @Disabled
    @TestGuiceyApp(Test5.App.class)
    public static class Test5 {

        @RecordLogs(DropwizardBundlesTracker.class)
        RecordedLogs logs;

        @Test
        void test() {
            Assertions.assertThat(logs.containing("Initialization time not tracked for bundles").count())
                    .isEqualTo(1);
        }

        public static class App extends DefaultTestApp {
            @Override
            public void initialize(Bootstrap<Configuration> bootstrap) {
                bootstrap.addBundle(new Bundle());
                bootstrap.addBundle(GuiceBundle.builder().printStartupTime().build());
            }
        }

        public static class Bundle implements ConfiguredBundle<Configuration> {

        }
    }

    @Disabled
    @TestGuiceyApp(Test6.App.class)
    public static class Test6 {

        @Test
        void test() {
        }

        public static class App extends DefaultTestApp {
            @Override
            public void initialize(Bootstrap<Configuration> bootstrap) {
                bootstrap.addBundle(GuiceBundle.builder().printStartupTime().build());
                bootstrap.addBundle(new ConfiguredBundle<Configuration>() {});
            }
        }
    }

    @Disabled
    @TestGuiceyApp(AutoScanApplication.class)
    public static class Test7 {

        @EnableHook
        static GuiceyConfigurationHook hook = builder -> builder
                .printStartupTime()
                .onGuiceyStartup((config, env, injector) -> env.lifecycle().manage(new Managed() {}));

        @Test
        void test() {
        }
    }



    @Override
    protected String clean(String out) {
        return out.replaceAll("\\$\\$Lambda\\$\\d+/\\d+(x[a-z\\d]+)?", "\\$\\$Lambda\\$111/1111111")
                // jdk 21
                .replaceAll("\\$\\$Lambda/\\d+(x[a-z\\d]+)?", "\\$\\$Lambda\\$111/1111111")

                .replaceAll("\\d+(\\.\\d+)? ms", "111 ms")

                // commands order may differ due to commands scan
                .replace("DummyCommand                       : 111 ms", "NonInjactableCommand               : 111 ms");
    }
}
