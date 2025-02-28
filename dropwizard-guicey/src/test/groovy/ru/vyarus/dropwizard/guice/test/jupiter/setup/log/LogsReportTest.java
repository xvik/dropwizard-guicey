package ru.vyarus.dropwizard.guice.test.jupiter.setup.log;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordLogs;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordedLogs;
import ru.vyarus.dropwizard.guice.test.jupiter.setup.log.support.LogRecordsApp;

/**
 * @author Vyacheslav Rusakov
 * @since 28.02.2025
 */
public class LogsReportTest extends AbstractPlatformTest {

    @Test
    void testDebugReport() {
        String out = run(Test1.class);
        Assertions.assertThat(out).contains("Applied log recorders (@RecordLogs) on LogsReportTest$Test1\n" +
                "\n" +
                "\t#logs                          WARN ");
    }

    @TestGuiceyApp(value = LogRecordsApp.class, debug = true)
    @Disabled
    public static class Test1 {

        @RecordLogs
        RecordedLogs logs;

        @Test
        void test() {

        }
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
