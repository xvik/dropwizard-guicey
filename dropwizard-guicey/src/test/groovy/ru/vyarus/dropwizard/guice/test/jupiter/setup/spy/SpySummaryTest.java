package ru.vyarus.dropwizard.guice.test.jupiter.setup.spy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean;

/**
 * @author Vyacheslav Rusakov
 * @since 18.02.2025
 */
public class SpySummaryTest extends AbstractSpyTest {

    @Test
    void testSpySummary() {

        String out = run(Test1.class);

        Assertions.assertThat(out).contains("@SpyBean stats on [After each] for SpySummaryTest$Test1#test():\n" +
                "\n" +
                "\t[Mockito] Interactions of: ru.vyarus.dropwizard.guice.test.jupiter.setup.spy.SpySummaryTest$Service$$EnhancerByGuice$$11111111@11111111\n" +
                "\t 1. spySummaryTest$Service$$EnhancerByGuice$$11111111.foo(\n" +
                "\t    1\n" +
                "\t);\n" +
                "\t  -> at ru.vyarus.dropwizard.guice.test.jupiter.setup.spy.SpySummaryTest$Test1.test(SpySummaryTest.java:50)");
    }


    @Test
    void testNoSpySummary() {

        String out = run(Test2.class);

        Assertions.assertThat(out).contains("@SpyBean stats on [After each] for SpySummaryTest$Test2#test():\n" +
                "\n" +
                "\tNo interactions and stubbings found for mock: ru.vyarus.dropwizard.guice.test.jupiter.setup.spy.SpySummaryTest$Service$$EnhancerByGuice$$11111111@11111111");
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test1 {

        @SpyBean(printSummary = true)
        Service service;

        @Test
        void test() {
            Assertions.assertThat(service.foo(1)).isEqualTo("foo1");
        }
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test2 {

        @SpyBean(printSummary = true)
        Service service;

        @Test
        void test() {
            // no mock actions
        }
    }

    public static class Service {
        public String foo(int i) {
            return "foo" + i;
        }
    }
}
