package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import com.google.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.track.Tracker;

/**
 * @author Vyacheslav Rusakov
 * @since 14.02.2025
 */
public class TrackerCleanupDisableTest extends AbstractTrackerTest {

    @Test
    void testNoTrackerCleanup() {
        String out = run(Test1.class);

        Assertions.assertThat(out).contains("Cleanup disabled: \n" +
                "\t[service]                                [method]                                           [calls]    [fails]    [min]      [max]      [median]   [75%]      [95%]     \n" +
                "\tService                                  foo()                                              2 (2)      0          11.11 ms   11.11 ms   11.11 ms   11.11 ms   11.11 ms");
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test1 {

        @Inject
        Service service;

        @TrackBean(autoReset = false)
        static Tracker<Service> tracker;

        @Test
        void testNoCleanup() {
            service.foo();
        }

        @Test
        void testNoCleanup2() {
            service.foo();
        }

        @AfterAll
        static void afterAll() {
            System.out.println("Cleanup disabled: \n" + tracker.getStats().render());
        }

        public static class Service {
            public String foo() {
                return "foo";
            }
        }
    }
}
