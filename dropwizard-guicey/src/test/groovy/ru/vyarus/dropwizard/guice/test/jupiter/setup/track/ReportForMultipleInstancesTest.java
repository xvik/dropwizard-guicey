package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.Tracker;

/**
 * @author Vyacheslav Rusakov
 * @since 14.02.2025
 */
public class ReportForMultipleInstancesTest extends AbstractTrackerTest {

    @Test
    void checkSummaryWithMultipleInstances() {

        String output = run(Test1.class);

        org.assertj.core.api.Assertions.assertThat(output)
                // warn
                .contains("\\\\\\---[Tracker<Service>] 11.11 ms      <@11111111> .foo(1) = \"foo1\"\n" +
                        "\\\\\\---[Tracker<Service>] 11.11 ms      <@11111111> .foo(2) = \"foo2\"")

                // 2 instances
                .contains("Tracker<Service> stats (sorted by median) for ReportForMultipleInstancesTest$Test1#testTracker():\n" +
                        "\n" +
                        "\t[service]                                [method]                                           [calls]    [fails]    [min]      [max]      [median]   [75%]      [95%]     \n" +
                        "\tService                                  foo(int)                                           2 (2)      0          11.11 ms   11.11 ms   11.11 ms   11.11 ms   11.11 ms");
    }


    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled // prevent direct execution
    public static class Test1 {

        @Inject
        Provider<Service> service;

        @TrackBean(trace = true, printSummary = true)
        Tracker<Test1.Service> track;

        @Test
        void testTracker() {
            Assertions.assertNotNull(track);
            service.get().foo(1);
            service.get().foo(2);
        }

        public static class Service {
            public String foo(int i) {
                return "foo" + i;
            }
        }
    }
}
