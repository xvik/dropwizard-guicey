package ru.vyarus.dropwizard.guice.test.jupiter.setup.log;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordLogs;
import ru.vyarus.dropwizard.guice.test.log.RecordedLogs;
import ru.vyarus.dropwizard.guice.test.log.support.DBundle1;
import ru.vyarus.dropwizard.guice.test.log.support.DBundleAfter;
import ru.vyarus.dropwizard.guice.test.log.support.DBundleBefore;
import ru.vyarus.dropwizard.guice.test.log.support.DManaged;
import ru.vyarus.dropwizard.guice.test.log.support.GBundle1;
import ru.vyarus.dropwizard.guice.test.log.support.GModule;
import ru.vyarus.dropwizard.guice.test.log.support.LogRecordsApp;

import java.util.Arrays;

/**
 * @author Vyacheslav Rusakov
 * @since 28.02.2025
 */
@TestGuiceyApp(LogRecordsApp.class)
public class LogsRecordCompletenessTest {

    @RecordLogs(loggers = "ru.vyarus.dropwizard.guice.test.log.support", level = Level.TRACE)
    RecordedLogs logs;

    @Test
    void testStartupLogsRecorded() {
        Assertions.assertEquals(13, logs.count());
        Assertions.assertEquals(13, logs.level(Level.TRACE).count());

        // impossible to detect run event (bundle runs after loggers reset and before guicey run)
        Assertions.assertEquals(Arrays.asList("Bundle initialized"),
                logs.logger(DBundleBefore.class).messages());

        Assertions.assertEquals(Arrays.asList("Bundle initialized", "Bundle started"),
                logs.logger(DBundle1.class).messages());
        Assertions.assertEquals(logs.logger(DBundle1.class).messages().size(),
                logs.logger(DBundle1.class).events().size());
        Assertions.assertEquals(logs.logger(DBundle1.class).level(Level.TRACE).count(),
                logs.logger(DBundle1.class).level(Level.TRACE).count());
        Assertions.assertTrue(logs.logger(DBundle1.class).has(Level.TRACE));

        Assertions.assertEquals(Arrays.asList("Bundle initialized", "Bundle started"),
                logs.logger(GBundle1.class).messages());

        Assertions.assertEquals(Arrays.asList("Bundle initialized", "Bundle started"),
                logs.logger(DBundleAfter.class).messages());

        Assertions.assertEquals(Arrays.asList("Managed started"),
                logs.logger(DManaged.class).messages());

        Assertions.assertEquals(Arrays.asList("Module configured"),
                logs.logger(GModule.class).messages());

        Assertions.assertEquals(Arrays.asList("Constructor", "Before init", "After init", "Run"),
                logs.logger(LogRecordsApp.class).messages());

        Assertions.assertEquals(Arrays.asList("Bundle started", "Bundle started", "Bundle started",
                "Managed started"), logs.containing("started").messages());

        Assertions.assertEquals(Arrays.asList("Bundle started"),
                logs.logger(DBundle1.class).containing("started").messages());
        Assertions.assertEquals(Arrays.asList("Bundle started"),
                logs.logger(DBundle1.class).matching("Bund.+ started").messages());
    }
}
