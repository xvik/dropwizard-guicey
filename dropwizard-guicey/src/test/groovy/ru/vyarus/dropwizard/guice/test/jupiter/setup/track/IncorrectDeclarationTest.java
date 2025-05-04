package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.track.Tracker;
import ru.vyarus.dropwizard.guice.test.track.TrackerConfig;

/**
 * @author Vyacheslav Rusakov
 * @since 14.02.2025
 */
public class IncorrectDeclarationTest extends AbstractTrackerTest {

    @Test
    void testIncorrectFieldType() {
        Throwable th = runFailed(Test1.class);
        Assertions.assertThat(th.getMessage()).isEqualTo(
                "Field IncorrectDeclarationTest$Test1.service annotated with @TrackBean, but its type is not Tracker");
    }

    @Test
    void testNoServiceType() {

        Throwable th = runFailed(Test2.class);
        Assertions.assertThat(th.getMessage()).isEqualTo(
                "Incorrect @TrackBean 'IncorrectDeclarationTest$Test2.service' declaration: " +
                        "tracked service must be declared as a tracker object generic: Tracker<Bean>");
    }

    @Test
    void testManualTracker() {

        Throwable th = runFailed(Test3.class);
        Assertions.assertThat(th.getMessage()).contains(
                "Incorrect @TrackBean 'IncorrectDeclarationTest$Test3.service' declaration: tracker instance can't be provided manually");
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test1 {

        @TrackBean
        Service service;

        @Test
        void test() {
            // no matter
        }
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test2 {

        @TrackBean
        Tracker service;

        @Test
        void test() {
            // no matter
        }
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test3 {

        @TrackBean
        static Tracker<Service> service = new Tracker<>(Service.class, new TrackerConfig(), null);

        @Test
        void test() {
            // no matter
        }
    }

    public static class Service {
    }
}
