package ru.vyarus.dropwizard.guice.test.jupiter.debug;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 25.06.2022
 */
public class NestedConfigOverrideLogTest extends AbstractPlatformTest {

    @Test
    void checkSetupOutputForAnnotation() {
        Test1.i = 1;
        String output = run(Test1.class);

        assertThat(output).contains("Guicey test extensions (Test1.Inner.test1.):");
        assertThat(output).contains("Configuration overrides (Test1.Inner.test1.):\n" +
                "\t                  foo = 1");

        assertThat(output).contains("Guicey test extensions (Test1.Inner.test2.):");
        assertThat(output).contains("Configuration overrides (Test1.Inner.test2.):\n" +
                "\t                  foo = 2");

        assertThat(output).contains(
                "Guicey time after [Before each] of Inner#test1(): 111 ms \n" +
                        "\n" +
                        "\t[Before each]                      : 111 ms \n" +
                        "\t\tGuicey fields search               : 111 ms \n" +
                        "\t\tGuicey hooks registration          : 111 ms \n" +
                        "\t\tGuicey setup objects execution     : 111 ms \n" +
                        "\t\tDropwizardTestSupport creation     : 111 ms \n" +
                        "\t\tApplication start                  : 111 ms \n" +
                        "\t\tGuice fields injection             : 111 ms");
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

    @Override
    protected String clean(String out) {
        return unifyMs(out);
    }
}
