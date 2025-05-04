package ru.vyarus.dropwizard.guice.test.log;

import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.log.support.LogRecordsApp;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2025
 */
public class NoLevelRiseTest {

    @Test
    void testLogsRecording() throws Exception {
        RecordLogsHook hook = new RecordLogsHook();
        final RecordedLogs logs = hook.record().start(Level.ERROR);

        TestSupport.build(LogRecordsApp.class)
                .hooks(hook)
                .runCore(injector -> {

                    Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                    Assertions.assertEquals(ch.qos.logback.classic.Level.INFO, logger.getLevel());
                    Assertions.assertEquals(0, logs.count());
                    return null;
                });
    }
}
