package ru.vyarus.dropwizard.guice.test.jupiter.debug;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 25.06.2022
 */
@ExtendWith(SystemStubsExtension.class)
public class ConfigOverrideLogTest {

    @SystemStub
    SystemOut out;

    @Test
    void checkSetupOutputForAnnotation() {
        TestSupport.debugExtensions();
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(Test1.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(1));

        String output = out.getText().replace("\r", "");
        System.err.println(output);
        output = output.replaceAll("\\d+\\.\\d+ ms", "111 ms");

        assertThat(output).contains("Applied configuration overrides (Test1.): \n" +
                "\n" +
                "\t                  foo = 1");

        assertThat(output).contains(
                "Guicey time after [After all] of ConfigOverrideLogTest$Test1: 111 ms ( + 111 ms)\n" +
                        "\n" +
                        "\t[Before all]                       : 111 ms\n" +
                        "\t\tGuicey fields search               : 111 ms\n" +
                        "\t\tGuicey hooks registration          : 111 ms\n" +
                        "\t\tDropwizardTestSupport creation     : 111 ms\n" +
                        "\t\tApplication start                  : 111 ms\n" +
                        "\n" +
                        "\t[Before each]                      : 111 ms\n" +
                        "\t\tGuice fields injection             : 111 ms\n" +
                        "\n" +
                        "\t[After each]                       : 111 ms\n" +
                        "\n" +
                        "\t[After all]                        : 111 ms\n" +
                        "\t\tApplication stop                   : 111 ms");
    }

    @Test
    void checkSetupOutputForManualRegistration() {
        TestSupport.debugExtensions();
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(Test2.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(1));

        String output = out.getText().replace("\r", "");
        System.err.println(output);
        output = output.replaceAll("\\d+\\.\\d+ ms", "111 ms");

        assertThat(output).contains("Applied configuration overrides (Test2.): \n" +
                "\n" +
                "\t                  foo = 2");

        assertThat(output).contains(
                "Guicey time after [After all] of ConfigOverrideLogTest$Test2: 111 ms ( + 111 ms)\n" +
                        "\n" +
                        "\t[Before all]                       : 111 ms\n" +
                        "\t\tGuicey fields search               : 111 ms\n" +
                        "\t\tGuicey hooks registration          : 111 ms\n" +
                        "\t\tDropwizardTestSupport creation     : 111 ms\n" +
                        "\t\tApplication start                  : 111 ms\n" +
                        "\n" +
                        "\t[Before each]                      : 111 ms\n" +
                        "\t\tGuice fields injection             : 111 ms\n" +
                        "\n" +
                        "\t[After each]                       : 111 ms\n" +
                        "\n" +
                        "\t[After all]                        : 111 ms\n" +
                        "\t\tApplication stop                   : 111 ms");
    }

    @Disabled // prevent direct execution
    @TestGuiceyApp(value = AutoScanApplication.class, configOverride = "foo: 1", debug = true)
    public static class Test1 {

        @Test
        void test() {
        }
    }

    @Disabled // prevent direct execution
    public static class Test2 {

        @RegisterExtension
        static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                .configOverrides("foo: 2")
                .debug()
                .create();

        @Test
        void test() {
        }

    }
}
