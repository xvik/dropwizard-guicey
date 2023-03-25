package ru.vyarus.dropwizard.guice.test.jupiter.setup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 17.05.2022
 */
public class SetupClosingTest {

    static Closable clos = new Closable();
    static Closable2 clos2 = new Closable2();

    @Test
    void checkClose() {
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(SetupClosingTest.Test1.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(1));

        Assertions.assertTrue(clos.called);
        Assertions.assertTrue(clos2.called);
    }


    @TestGuiceyApp(AutoScanApplication.class)
    @Disabled // prevent direct execution
    public static class Test1 {

        @EnableSetup
        static TestEnvironmentSetup ext = it -> clos;

        @EnableSetup
        static TestEnvironmentSetup ext2 = it -> clos2;

        @Test
        void fooTest() {
        }
    }

    public static class Closable implements AutoCloseable {

        public boolean called;

        @Override
        public void close() throws Exception {
            called = true;
        }
    }

    public static class Closable2 implements ExtensionContext.Store.CloseableResource {

        public boolean called;

        @Override
        public void close() throws Exception {
            called = true;
        }
    }
}
