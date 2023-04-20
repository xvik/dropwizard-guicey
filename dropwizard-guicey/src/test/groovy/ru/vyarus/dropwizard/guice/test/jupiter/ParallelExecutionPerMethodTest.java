package ru.vyarus.dropwizard.guice.test.jupiter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;

import javax.inject.Inject;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 18.06.2022
 */
public class ParallelExecutionPerMethodTest {

    @Test
    void checkParallelExecution() {
//        TestSupport.debugExtensions();
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
                .configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent")
                .configurationParameter("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
                .selectors(selectClass(Test1.class),
                        selectClass(Test2.class),
                        selectClass(Test3.class),
                        selectClass(Test4.class))
                .execute()
                .testEvents()
                .assertStatistics(stats -> stats.succeeded(16));
    }

    @Disabled // prevent direct execution
    public static class Test1 {

        @RegisterExtension
        TestDropwizardAppExtension ext = TestDropwizardAppExtension.forApp(AutoScanApplication.class)
                .randomPorts()
                .configOverrides("foo: 1")
                .create();

        @Inject
        TestConfiguration config;

        @Test
        void check() {
            Assertions.assertEquals(1, config.foo);
        }

        @Test
        void check2() {
            Assertions.assertEquals(1, config.foo);
        }

        @Test
        void check3() {
            Assertions.assertEquals(1, config.foo);
        }

        @Test
        void check4() {
            Assertions.assertEquals(1, config.foo);
        }
    }

    @Disabled // prevent direct execution
    public static class Test2 {

        @RegisterExtension
        TestDropwizardAppExtension ext = TestDropwizardAppExtension.forApp(AutoScanApplication.class)
                .randomPorts()
                .configOverrides("foo: 2")
                .create();

        @Inject
        TestConfiguration config;

        @Test
        void check() {
            Assertions.assertEquals(2, config.foo);
        }

        @Test
        void check2() {
            Assertions.assertEquals(2, config.foo);
        }

        @Test
        void check3() {
            Assertions.assertEquals(2, config.foo);
        }

        @Test
        void check4() {
            Assertions.assertEquals(2, config.foo);
        }
    }

    @Disabled // prevent direct execution
    public static class Test3 {

        @RegisterExtension
        TestDropwizardAppExtension ext = TestDropwizardAppExtension.forApp(AutoScanApplication.class)
                .randomPorts()
                .configOverrides("foo: 3")
                .create();

        @Inject
        TestConfiguration config;

        @Test
        void check() {
            Assertions.assertEquals(3, config.foo);
        }

        @Test
        void check2() {
            Assertions.assertEquals(3, config.foo);
        }

        @Test
        void check3() {
            Assertions.assertEquals(3, config.foo);
        }

        @Test
        void check4() {
            Assertions.assertEquals(3, config.foo);
        }
    }

    @Disabled // prevent direct execution
    public static class Test4 {

        @RegisterExtension
        TestDropwizardAppExtension ext = TestDropwizardAppExtension.forApp(AutoScanApplication.class)
                .randomPorts()
                .configOverrides("foo: 4")
                .create();

        @Inject
        TestConfiguration config;

        @Test
        void check() {
            Assertions.assertEquals(4, config.foo);
        }

        @Test
        void check2() {
            Assertions.assertEquals(4, config.foo);
        }

        @Test
        void check3() {
            Assertions.assertEquals(4, config.foo);
        }

        @Test
        void check4() {
            Assertions.assertEquals(4, config.foo);
        }
    }
}
