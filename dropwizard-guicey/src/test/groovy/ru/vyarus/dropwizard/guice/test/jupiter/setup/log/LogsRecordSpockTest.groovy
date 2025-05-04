package ru.vyarus.dropwizard.guice.test.jupiter.setup.log

import io.dropwizard.lifecycle.Managed
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.support.DefaultTestApp
import ru.vyarus.dropwizard.guice.test.EnableHook
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordLogs
import ru.vyarus.dropwizard.guice.test.log.RecordedLogs
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 26.03.2025
 */
@TestGuiceyApp(value = DefaultTestApp, debug = true)
class LogsRecordSpockTest extends Specification {

    @EnableHook
    static GuiceyConfigurationHook hook = builder -> builder.extensions(Service)

    @RecordLogs(value = Service, level = Level.DEBUG)
    RecordedLogs logs

    @Inject
    Service service

    def "Check log records"() {

        expect:
        logs
        2 == logs.count()

        when:
        service.foo()

        then:
        3 == logs.count()
        logs.has(Level.WARN)
        1 == logs.level(Level.WARN).count()
    }

    @Singleton
    static class Service implements Managed {
        private final Logger logger = LoggerFactory.getLogger(Service)

        Service() {
            logger.debug("Created Service {}", "smth")
        }

        @Override
        void start() throws Exception {
            logger.info("Start")
        }

        @Override
        void stop() throws Exception {
            logger.info("Stop")
        }

        void foo() {
            logger.warn("Foo called")
        }
    }
}
