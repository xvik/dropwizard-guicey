package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.Tracker;

import java.time.temporal.ChronoUnit;

/**
 * @author Vyacheslav Rusakov
 * @since 14.02.2025
 */

public class SlowMethodWarnTest extends AbstractTrackerTest {

    @Test
    void checkSlowMethodWarning() {

        String output = run(Test1.class);

        org.assertj.core.api.Assertions.assertThat(output)
                // warn
                .contains("WARN  [2025-22-22 11:11:11] ru.vyarus.dropwizard.guice.test.jupiter.ext.track.Tracker: \n" +
                        "\\\\\\---[Tracker<Service>] 11.11 ms      <@11111111> .foo() = \"foo\"");
    }

    @Test
    void checkSlowMethodWarningDisabled() {

        String output = run(Test2.class);

        org.assertj.core.api.Assertions.assertThat(output)
                .doesNotContain("WARN  [2025-22-22 11:11:11] ru.vyarus.dropwizard.guice.test.jupiter.ext.track.Tracker: \n" +
                        "\\\\\\---[Tracker<Service>] 11.11 ms      <@11111111> .foo() = \"foo\"");
    }


    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled // prevent direct execution
    public static class Test1 {

        @Inject Service service;

        @TrackBean(slowMethods = 1, slowMethodsUnit = ChronoUnit.MILLIS)
        Tracker<Service> track;

        @Test
        void testTracker() {
            Assertions.assertNotNull(track);
            service.foo();
        }
    }


    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled // prevent direct execution
    public static class Test2 {

        @Inject Service service;

        @TrackBean(slowMethods = 0, slowMethodsUnit = ChronoUnit.MILLIS)
        Tracker<Service> track;

        @Test
        void testTracker() {
            Assertions.assertNotNull(track);
            service.foo();
        }
    }

    public static class Service {
        public String foo() {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "foo";
        }
    }
}
