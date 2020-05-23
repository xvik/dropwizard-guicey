package ru.vyarus.dropwizard.guice.test.jupiter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.ClientSupport;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 15.05.2020
 */
public class ParallelExecutionTest {

    @Test
    void checkParallelExecution() {
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
                .configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent")
                .selectors(selectClass(Test1.class),
                        selectClass(Test2.class),
                        selectClass(Test3.class),
                        selectClass(Test4.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(4));
    }

    @TestDropwizardApp(value = AutoScanApplication.class, configOverride = {
            "server.applicationConnectors[0].port: 20000",
            "server.adminConnectors[0].port: 20001"
    })
    public static class Test1 {

        @Test
        void check(ClientSupport client) {
            Assertions.assertEquals(20000, client.getPort());
        }
    }

    @TestDropwizardApp(value = AutoScanApplication.class, configOverride = {
            "server.applicationConnectors[0].port: 10000",
            "server.adminConnectors[0].port: 10001"
    })
    public static class Test2 {

        @Test
        void check(ClientSupport client) {
            Assertions.assertEquals(10000, client.getPort());
        }
    }

    @TestGuiceyApp(value = AutoScanApplication.class, configOverride = {
            "foo: 1"
    })
    public static class Test3 {

        @Test
        void check(TestConfiguration config) {
            Assertions.assertEquals(1, config.foo);
        }
    }

    @TestGuiceyApp(value = AutoScanApplication.class, configOverride = {
            "foo: 2"
    })
    public static class Test4 {

        @Test
        void check(TestConfiguration config) {
            Assertions.assertEquals(2, config.foo);
        }
    }
}
