package ru.vyarus.dropwizard.guice.test.log;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.log.support.DBundle1;
import ru.vyarus.dropwizard.guice.test.log.support.GBundle1;
import ru.vyarus.dropwizard.guice.test.log.support.LogRecordsApp;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2025
 */
public class MixedRecorderTest {

    @Test
    void testLogsRecording() throws Exception {
        RecordLogsHook hook = new RecordLogsHook();
        final RecordedLogs logs = hook.record()
                .loggers(DBundle1.class)
                .loggers(GBundle1.class.getName())
                .start(Level.TRACE);

        TestSupport.build(LogRecordsApp.class)
                .hooks(hook)
                .runCore(injector -> {

                    Assertions.assertEquals(2, logs.logger(DBundle1.class).count());
                    Assertions.assertEquals(2, logs.logger(GBundle1.class).count());

                    return null;
                });
    }

}
