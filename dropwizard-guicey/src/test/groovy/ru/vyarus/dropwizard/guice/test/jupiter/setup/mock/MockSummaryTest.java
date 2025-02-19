package ru.vyarus.dropwizard.guice.test.jupiter.setup.mock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean;

/**
 * @author Vyacheslav Rusakov
 * @since 18.02.2025
 */
public class MockSummaryTest extends AbstractMockTest {

    @Test
    void testMockSummary() {

        String out = run(Test1.class);

        Assertions.assertThat(out).contains("@MockBean stats on [After each] for MockSummaryTest$Test1#test():\n" +
                "\n" +
                "\t[Mockito] Interactions of: Mock for Service, hashCode: 11111111\n" +
                "\t 1. service.foo(1);\n" +
                "\t  -> at ru.vyarus.dropwizard.guice.test.jupiter.setup.mock.MockSummaryTest$Test1.test(MockSummaryTest.java:55)\n" +
                "\t   - stubbed -> at ru.vyarus.dropwizard.guice.test.jupiter.setup.mock.MockSummaryTest$Test1.setUp(MockSummaryTest.java:50)");
    }

    @Test
    void testNoMockSummary() {

        String out = run(Test2.class);

        Assertions.assertThat(out).contains("@MockBean stats on [After each] for MockSummaryTest$Test2#test():\n" +
                "\n" +
                "\tNo interactions and stubbings found for mock: Mock for Service, hashCode: 11111111");
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test1 {

        @MockBean(printSummary = true)
        Service service;

        @BeforeEach
        void setUp() {
            Mockito.when(service.foo(Mockito.anyInt())).thenReturn("bar");
        }

        @Test
        void test() {
            Assertions.assertThat(service.foo(1)).isEqualTo("bar");
        }
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test2 {

        @MockBean(printSummary = true)
        Service service;

        @Test
        void test() {
            // mock not used
        }
    }

    public static class Service {
        public String foo(int i) {
            return "foo" + i;
        }
    }
}
