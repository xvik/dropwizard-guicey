package ru.vyarus.dropwizard.guice.test.jupiter.setup.log;

import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordLogs;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordedLogs;
import ru.vyarus.dropwizard.guice.test.jupiter.setup.log.support.LogRecordsApp;

/**
 * @author Vyacheslav Rusakov
 * @since 28.02.2025
 */
@TestGuiceyApp(LogRecordsApp.class)
public class NotRiseLevelTest {

    @RecordLogs(level = Level.ERROR)
    RecordedLogs logs;

    @Test
    void testLoggerLevelNotChanged() {
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Assertions.assertEquals(ch.qos.logback.classic.Level.INFO, logger.getLevel());
        Assertions.assertEquals(0, logs.count());
    }
}
