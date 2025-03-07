package ru.vyarus.dropwizard.guice.test.jupiter.debug;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 07.03.2025
 */
public class PerformanceLogTest extends AbstractPlatformTest {

    @Test
    void testIterativePerformanceLog() {

        String output = run(Test1.class);

        Assertions.assertThat(output).contains("Guicey time after [Before each] of PerformanceLogTest$Test1#test1(): 111 ms\n" +
                "\n" +
                "\t[Before all]                       : 111 ms\n" +
                "\t\tGuicey fields search               : 111 ms\n" +
                "\t\tGuicey hooks registration          : 111 ms\n" +
                "\t\tGuicey setup objects execution     : 111 ms\n" +
                "\t\tDropwizardTestSupport creation     : 111 ms\n" +
                "\t\tApplication start                  : 111 ms\n" +
                "\t\tListeners execution                : 111 ms\n" +
                "\n" +
                "\t[Before each]                      : 111 ms\n" +
                "\t\tGuice fields injection             : 111 ms\n" +
                "\t\tListeners execution                : 111 ms");

        Assertions.assertThat(output).contains("Guicey time after [Before each] of PerformanceLogTest$Test1#test2(): 111 ms ( + 111 ms)\n" +
                "\n" +
                "\t[Before each]                      : 111 ms ( + 111 ms)\n" +
                "\t\tGuice fields injection             : 111 ms ( + 111 ms)\n" +
                "\t\tListeners execution                : 111 ms ( + 111 ms)\n" +
                "\n" +
                "\t[After each]                       : 111 ms\n" +
                "\t\tListeners execution                : 111 ms");

        Assertions.assertThat(output).contains("Guicey time after [After all] of PerformanceLogTest$Test1: 111 ms ( + 111 ms)\n" +
                "\n" +
                "\t[After each]                       : 111 ms ( + 111 ms)\n" +
                "\t\tListeners execution                : 111 ms ( + 111 ms)\n" +
                "\n" +
                "\t[After all]                        : 111 ms\n" +
                "\t\tApplication stop                   : 111 ms\n" +
                "\t\tListeners execution                : 111 ms");
    }

    @Disabled
    @TestGuiceyApp(value = AutoScanApplication.class, debug = true)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public static class Test1 {

        @Test
        @Order(1)
        void test1() {
        }

        @Test
        @Order(1)
        void test2() {
        }
    }

    @Override
    protected String clean(String out) {
        return out.replaceAll("\\d+\\.\\d+ ms", "111 ms");
    }
}
