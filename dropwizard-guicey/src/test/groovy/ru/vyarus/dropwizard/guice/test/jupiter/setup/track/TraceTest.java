package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
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
public class TraceTest extends AbstractTrackerTest {
    @Test
    void checkTrace() {

        String output = run(Test1.class);

        org.assertj.core.api.Assertions.assertThat(output)
                // warn
                .contains("\\\\\\---[Tracker<Service>] 11.11 ms      <@11111111> .foo(1) = \"foo1\"\n" +
                        "\\\\\\---[Tracker<Service>] 11.11 ms      <@11111111> .foo(2) = \"foo2\"");
    }


    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled // prevent direct execution
    public static class Test1 {

        @Inject
        Test1.Service service;

        @TrackBean(trace = true)
        Tracker<Test1.Service> track;

        @Test
        void testTracker() {
            Assertions.assertNotNull(track);
            service.foo(1);
            service.foo(2);
        }

        public static class Service {
            public String foo(int i) {
                return "foo" + i;
            }
        }
    }
}
