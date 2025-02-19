package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import com.google.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.Tracker;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.stat.MethodSummary;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.stat.TrackerStats;

/**
 * @author Vyacheslav Rusakov
 * @since 19.02.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
public class StatsTest {

    @Inject
    Service service;

    @TrackBean
    Tracker<Service> tracker;

    @BeforeEach
    void setUp() {
        final TrackerStats stats = tracker.getStats();

        Assertions.assertNotNull(stats);
        Assertions.assertEquals(0, stats.getMethods().size());
        Assertions.assertNull(stats.render());
    }

    @Test
    void testMethodCall() {
        service.foo();
    }

    @AfterEach
    void tearDown() {
        final TrackerStats stats = tracker.getStats();

        Assertions.assertNotNull(stats);
        Assertions.assertEquals(1, stats.getMethods().size());
        Assertions.assertNotNull(stats.render());
        MethodSummary summary = stats.getMethods().get(0);
        Assertions.assertEquals("foo", summary.getMethod().getName());
        Assertions.assertEquals(Service.class, summary.getService());
        Assertions.assertEquals(1, summary.getTracks());
        Assertions.assertEquals(0, summary.getErrors());
        Assertions.assertEquals(1, summary.getMetrics().getValues().length);
        Assertions.assertNotNull(summary.getMin());
        Assertions.assertNotNull(summary.getMax());
        Assertions.assertNotNull(summary.getMedian());
        Assertions.assertNotNull(summary.get75thPercentile());
        Assertions.assertNotNull(summary.get95thPercentile());
        Assertions.assertEquals("foo() called 1 times", summary.toString());
    }

    public static class Service {
        public void foo(){}
    }
}
