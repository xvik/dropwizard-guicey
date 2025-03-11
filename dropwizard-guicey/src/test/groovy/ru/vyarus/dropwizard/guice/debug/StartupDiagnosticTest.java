package ru.vyarus.dropwizard.guice.debug;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
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
                "\t\t\t\t\tDummyCommand                       : 111 ms\n" +
                "\t\t\t\t\tNonInjactableCommand               : 111 ms\n" +
                "\t\t\t\tBundles lookup                     : 111 ms\n" +
                "\t\t\t\tGuicey bundles init                : 111 ms\n" +
                "\t\t\t\t\tCoreInstallersBundle               : 111 ms\n" +
                "\t\t\t\t\tWebInstallersBundle                : 111 ms\n" +
                "\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\t\tInstallers resolution              : 111 ms\n" +
                "\t\t\t\t\tExtensions recognition             : 111 ms\n" +
                "\t\t\t\tListeners time                     : 111 ms\n" +
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
                "\t\t\t\t\tGuice bindings analysis            : 111 ms\n" +
                "\t\t\t\t\tExtensions installation            : 111 ms\n" +
                "\t\t\t\tInjector creation                  : 111 ms\n");

        Assertions.assertThat(out).contains("\t\t\t\tListeners time                     : 111 ms\n" +
                        "\n" +
                        "\t\tWeb server startup                 : 111 ms\n" +
                        "\t\t\tLifecycle time                     : 111 ms\n" +
                        "\t\t\t\tmanaged   ExecutorServiceManager             : 111 ms\n" +
                        "\t\t\t\tmanaged   RegistryShutdown                   : 111 ms\n" +
                        "\t\t\t\tmanaged   DummyManaged                       : 111 ms\n" +
                        "\t\t\tGuicey time                        : 111 ms\n" +
                        "\t\t\t\tInstallers time                    : 111 ms\n" +
                        "\t\t\t\tListeners time                     : 111 ms");

        Assertions.assertThat(out).contains("Application shutdown time: \n" +
                "\n" +
                "\tApplication shutdown               : 111 ms\n" +
                "\t\tmanaged   DummyManaged                       : 111 ms\n" +
                "\t\tmanaged   RegistryShutdown                   : 111 ms\n" +
                "\t\tmanaged   ExecutorServiceManager             : 111 ms");
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
                "\t\t\t\t\tDummyCommand                       : 111 ms\n" +
                "\t\t\t\t\tNonInjactableCommand               : 111 ms\n" +
                "\t\t\t\tBundles lookup                     : 111 ms\n" +
                "\t\t\t\tGuicey bundles init                : 111 ms\n" +
                "\t\t\t\t\tCoreInstallersBundle               : 111 ms\n" +
                "\t\t\t\t\tWebInstallersBundle                : 111 ms\n" +
                "\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\t\tInstallers resolution              : 111 ms\n" +
                "\t\t\t\t\tExtensions recognition             : 111 ms\n" +
                "\t\t\t\tListeners time                     : 111 ms\n" +
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
                "\t\t\t\t\tGuice bindings analysis            : 111 ms\n" +
                "\t\t\t\t\tExtensions installation            : 111 ms\n" +
                "\t\t\t\tInjector creation                  : 111 ms\n");

        Assertions.assertThat(out).contains("\t\t\t\tListeners time                     : 111 ms\n" +
                "\n" +
                "\t\tWeb server startup                 : 111 ms\n" +
                "\t\t\tLifecycle time                     : 111 ms\n" +
                "\t\t\t\tmanaged   ExecutorServiceManager             : 111 ms\n" +
                "\t\t\t\tmanaged   RegistryShutdown                   : 111 ms\n" +
                "\t\t\t\tmanaged   DummyManaged                       : 111 ms\n" +
                "\t\t\tGuicey time                        : 111 ms\n" +
                "\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\tListeners time                     : 111 ms");

        Assertions.assertThat(out).contains("Application shutdown time: \n" +
                "\n" +
                "\tApplication shutdown               : 111 ms\n" +
                "\t\tmanaged   DummyManaged                       : 111 ms\n" +
                "\t\tmanaged   RegistryShutdown                   : 111 ms\n" +
                "\t\tmanaged   ExecutorServiceManager             : 111 ms");
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
                "\t\t\t\t\tDummyCommand                       : 111 ms\n" +
                "\t\t\t\t\tNonInjactableCommand               : 111 ms\n" +
                "\t\t\t\tBundles lookup                     : 111 ms\n" +
                "\t\t\t\tGuicey bundles init                : 111 ms\n" +
                "\t\t\t\t\tCoreInstallersBundle               : 111 ms\n" +
                "\t\t\t\t\tWebInstallersBundle                : 111 ms\n" +
                "\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\t\tInstallers resolution              : 111 ms\n" +
                "\t\t\t\t\tExtensions recognition             : 111 ms\n" +
                "\t\t\t\tListeners time                     : 111 ms\n" +
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
                "\t\t\t\t\tGuice bindings analysis            : 111 ms\n" +
                "\t\t\t\t\tExtensions installation            : 111 ms\n" +
                "\t\t\t\tInjector creation                  : 111 ms\n");

        Assertions.assertThat(out).contains("\t\t\t\tListeners time                     : 111 ms\n" +
                "\n" +
                "\t\tWeb server startup                 : 111 ms\n" +
                "\t\t\tLifecycle time                     : 111 ms\n" +
                "\t\t\t\tmanaged   ExecutorServiceManager             : 111 ms\n" +
                "\t\t\t\tmanaged   RegistryShutdown                   : 111 ms\n" +
                "\t\t\t\tmanaged   DummyManaged                       : 111 ms\n" +
                "\t\t\tGuicey time                        : 111 ms\n" +
                "\t\t\t\tInstallers time                    : 111 ms\n" +
                "\t\t\t\tListeners time                     : 111 ms");

        Assertions.assertThat(out).contains("Application shutdown time: \n" +
                "\n" +
                "\tApplication shutdown               : 111 ms\n" +
                "\t\tmanaged   DummyManaged                       : 111 ms\n" +
                "\t\tmanaged   RegistryShutdown                   : 111 ms\n" +
                "\t\tmanaged   ExecutorServiceManager             : 111 ms");
    }

    @Disabled
    @TestGuiceyApp(AutoScanApplication.class)
    public static class Test1 {

        @EnableHook
        static GuiceyConfigurationHook hook = GuiceBundle.Builder::printStartupTime;

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


    @Override
    protected String clean(String out) {
        return out.replaceAll("\\$\\$Lambda\\$\\d+/\\d+(x[a-z\\d]+)?", "\\$\\$Lambda\\$111/1111111")
                // jdk 21
                .replaceAll("\\$\\$Lambda/\\d+(x[a-z\\d]+)?", "\\$\\$Lambda\\$111/1111111")
                
                .replaceAll("\\d+(\\.\\d+)? ms", "111 ms");
    }
}
