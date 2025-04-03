package ru.vyarus.dropwizard.guice.test.jupiter.setup.log;

import io.dropwizard.lifecycle.Managed;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordLogs;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordedLogs;

/**
 * @author Vyacheslav Rusakov
 * @since 26.02.2025
 */
@TestGuiceyApp(value = DefaultTestApp.class, debug = true)
public class LogsRecordSimpleTest {

    @EnableHook
    static GuiceyConfigurationHook hook = builder -> builder.extensions(Service.class);

    @RecordLogs(value = Service.class, level = Level.DEBUG)
    RecordedLogs logs;

    @Inject
    Service service;

    @Test
    void test() {

        Assertions.assertNotNull(logs);
        Assertions.assertEquals(2, logs.count());
        Assertions.assertEquals(logs.events().size(), logs.messages().size());
        Assertions.assertEquals(2, logs.logger(Service.class).count());
        Assertions.assertTrue(logs.has(Level.DEBUG));
        Assertions.assertTrue(logs.has(Level.INFO));
        Assertions.assertEquals(1, logs.level(Level.DEBUG).count());
        Assertions.assertEquals(1, logs.level(Level.INFO).count());

        service.foo();
        Assertions.assertEquals(3, logs.count());
        Assertions.assertTrue(logs.has(Level.WARN));
        Assertions.assertEquals(1, logs.level(Level.WARN).count());
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
