package ru.vyarus.dropwizard.guice.test.jupiter.debug;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.TestSupport;
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
public class NestedConfigOverrideLogTest {


    @SystemStub
    SystemOut out;

    @Test
    void checkSetupOutputForAnnotation() {
        Test1.i = 1;
        TestSupport.debugExtensions();
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(Test1.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(2));

        String output = out.getText().replace("\r", "");
        System.err.println(output);
        output = output.replaceAll("\\d+\\.\\d+ ms", "111 ms");

        assertThat(output).contains("Guicey test extensions (Test1.Inner.test1.):");
        assertThat(output).contains("Applied configuration overrides (Test1.Inner.test1.): \n" +
                "\n" +
                "\t                  foo = 1");

        assertThat(output).contains("Guicey test extensions (Test1.Inner.test2.):");
        assertThat(output).contains("Applied configuration overrides (Test1.Inner.test2.): \n" +
                "\n" +
                "\t                  foo = 2");

        assertThat(output).contains(
                "Guicey time after [After each] of Inner#test1(): 111 ms ( + 111 ms)\n" +
                        "\n" +
                        "\t[Before each]                      : 111 ms\n" +
                        "\t\tGuicey fields search               : 111 ms\n" +
                        "\t\tGuicey hooks registration          : 111 ms\n" +
                        "\t\tGuicey setup objects execution     : 111 ms\n" +
                        "\t\tDropwizardTestSupport creation     : 111 ms\n" +
                        "\t\tApplication start                  : 111 ms\n" +
                        "\t\tListeners execution                : 111 ms\n" +
                        "\t\tGuice fields injection             : 111 ms\n" +
                        "\n" +
                        "\t[After each]                       : 111 ms\n" +
                        "\t\tApplication stop                   : 111 ms\n" +
                        "\t\tListeners execution                : 111 ms\n");
    }

    public static class Test1 {
        static int i;

        @RegisterExtension
        TestGuiceyAppExtension extension = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                // setup object used only to check log
                .setup(ext -> ext.configOverride("foo", () -> String.valueOf(i++)))
                .debug()
                .create();

        @Nested
        class Inner {

            @Test
            void test1() {}

            @Test
            void test2() {}
        }
    }
}
