package ru.vyarus.dropwizard.guice.test.log;

import io.dropwizard.lifecycle.Managed;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2025
 */
public class LogsRecordTest {

    @Test
    void testLogsRecording() throws Exception {
        RecordLogsHook hook = new RecordLogsHook();
        final RecordedLogs logs = hook.record().loggers(Service.class).start(Level.DEBUG);

        TestSupport.build(App.class)
                .hooks(hook)
                .runCore(injector -> {

                    Assertions.assertNotNull(logs);
                    Assertions.assertEquals(2, logs.count());
                    Assertions.assertEquals(logs.events().size(), logs.messages().size());
                    Assertions.assertEquals(2, logs.logger(Service.class).count());
                    Assertions.assertTrue(logs.has(Level.DEBUG));
                    Assertions.assertTrue(logs.has(Level.INFO));
                    Assertions.assertEquals(1, logs.level(Level.DEBUG).count());
                    Assertions.assertEquals(1, logs.level(Level.INFO).count());

                    Service service = injector.getInstance(Service.class);
                    service.foo();
                    Assertions.assertEquals(3, logs.count());
                    Assertions.assertTrue(logs.has(Level.WARN));
                    Assertions.assertEquals(1, logs.level(Level.WARN).count());

                    hook.clearLogs();
                    Assertions.assertEquals(0, logs.count());

            return null;
        });
    }

    @Test
    void testMultipleRecordings() throws Exception {
        RecordLogsHook hook = new RecordLogsHook();
        final RecordedLogs logs = hook.record().loggers(Service.class).start(Level.DEBUG);
        final RecordedLogs logs2 = hook.record().loggers(Service.class).start(Level.DEBUG);

        TestSupport.build(App.class)
                .hooks(hook)
                .runCore(injector -> {

                    Assertions.assertEquals(2, logs.count());
                    Assertions.assertEquals(2, logs2.count());

                    Service service = injector.getInstance(Service.class);
                    service.foo();

                    Assertions.assertEquals(3, logs.count());
                    Assertions.assertEquals(3, logs2.count());

                    return null;
                });
    }

    @Test
    void testRecorderDestroy() throws Exception {
        RecordLogsHook hook = new RecordLogsHook();
        final RecordedLogs logs = hook.record().loggers(Service.class).start(Level.DEBUG);

        TestSupport.build(App.class)
                .hooks(hook)
                .runCore(injector -> {

                    Assertions.assertEquals(2, logs.count());

                    hook.clearLogs();
                    hook.destroy();

                    Service service = injector.getInstance(Service.class);
                    service.foo();

                    Assertions.assertEquals(0, logs.count());

                    return null;
                });
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(Service.class)
                    .build();
        }
    }

    @Singleton
    public static class Service implements Managed {
        private final Logger logger = LoggerFactory.getLogger(Service.class);

        public Service() {
            logger.debug("Created Service {}", "smth");
        }

        @Override
        public void start() throws Exception {
            logger.info("Start");
        }

        @Override
        public void stop() throws Exception {
            logger.info("Stop");
        }

        public void foo() {
            logger.warn("Foo called");
        }
    }
}
