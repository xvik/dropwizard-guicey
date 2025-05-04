package ru.vyarus.dropwizard.guice.test.jupiter.setup.log;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordLogs;
import ru.vyarus.dropwizard.guice.test.log.RecordedLogs;
import ru.vyarus.dropwizard.guice.test.log.support.LogRecordsApp;

/**
 * @author Vyacheslav Rusakov
 * @since 28.02.2025
 */
public class LogsCleanupTest extends AbstractPlatformTest {

    @Test
    void testAutoReset() {
        runSuccess(Test1.class, Test2.class);
    }

    @TestGuiceyApp(LogRecordsApp.class)
    @Disabled
    public static class Test1 {

        @RecordLogs(loggers = "ru.vyarus.dropwizard.guice.test.log.support", level = Level.TRACE)
        static RecordedLogs logs;

        @Test
        void test() {
            Assertions.assertFalse(logs.empty());
        }

        @AfterAll
        static void afterAll() {
            Assertions.assertTrue(logs.empty());
        }
    }

    @TestGuiceyApp(LogRecordsApp.class)
    @Disabled
    public static class Test2 {

        @RecordLogs(loggers = "ru.vyarus.dropwizard.guice.test.log.support",
                level = Level.TRACE, autoReset = false)
        static RecordedLogs logs;

        @Test
        void test() {
            Assertions.assertFalse(logs.empty());
        }

        @AfterAll
        static void afterAll() {
            Assertions.assertFalse(logs.empty());
        }
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
