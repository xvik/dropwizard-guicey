package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.track.MethodTrack;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.track.Tracker;

import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author Vyacheslav Rusakov
 * @since 11.02.2025
 */
@TestGuiceyApp(value = DefaultTestApp.class, debug = true)
public class TrackerSimpleTest {

    @Inject
    Service service;

    @TrackBean(trace = true)
    Tracker<Service> serviceTracker;

    @Test
    void testTracker() {
        Assertions.assertNotNull(serviceTracker);

        // call service
        Assertions.assertEquals("1 call", service.foo(1));

        Assertions.assertEquals(Service.class, serviceTracker.getType());
        Assertions.assertEquals(1, serviceTracker.size());
        Assertions.assertEquals(1, serviceTracker.getTracks().size());
        Assertions.assertFalse(serviceTracker.isEmpty());
        MethodTrack track = serviceTracker.getLastTrack();
        Assertions.assertTrue(track.toString().contains("foo(1) = \"1 call\""));
        Assertions.assertArrayEquals(new Object[]{1}, track.getRawArguments());
        Assertions.assertArrayEquals(new String[]{"1"}, track.getArguments());
        Assertions.assertEquals("1 call", track.getRawResult());
        Assertions.assertEquals("1 call", track.getResult());
        Assertions.assertEquals("foo", track.getMethod().getName());
        Assertions.assertEquals(Service.class, track.getService());
        Assertions.assertTrue(track.getStarted() > 0);
        Assertions.assertNotNull(track.getDuration());
        Assertions.assertNotNull(track.getInstanceHash());


        // call more
        Assertions.assertEquals("2 call", service.foo(2));
        Assertions.assertEquals("1 bar", service.bar(1));


        Assertions.assertEquals(3, serviceTracker.getTracks().size());
        List<MethodTrack> tracks = serviceTracker.getLastTracks(2);
        Assertions.assertEquals("foo(2) = \"2 call\"", tracks.get(0).toStringTrack());
        Assertions.assertEquals("bar(1) = \"1 bar\"", tracks.get(1).toStringTrack());


        // search with mockito api
        tracks = serviceTracker.findTracks(mock -> when(
                mock.foo(Mockito.anyInt()))
        );
        Assertions.assertEquals(2, tracks.size());

        // few more calls (to check mocks correct reset)
        Assertions.assertEquals("foo", tracks.get(0).getMethod().getName());
        Assertions.assertEquals("foo", tracks.get(1).getMethod().getName());

        tracks = serviceTracker.findTracks(mock -> when(
                mock.foo(Mockito.intThat(argument -> argument == 1)))
        );
        Assertions.assertEquals(1, tracks.size());
        Assertions.assertEquals(1, tracks.get(0).getRawArguments()[0]);

        // and another call to make sure results not cached
        Assertions.assertEquals("1 call", service.foo(1));

        tracks = serviceTracker.findTracks(mock -> when(
                mock.foo(Mockito.intThat(argument -> argument == 1)))
        );
        Assertions.assertEquals(2, tracks.size());
    }

    @Test
    void testVoidMethod() {
        service.baz("small");

        MethodTrack track = serviceTracker.getLastTrack();
        Assertions.assertTrue(track.toString().contains("baz(\"small\")"));
        Assertions.assertNull(track.getResult());
        Assertions.assertNull(track.getRawResult());
        Assertions.assertNull(track.getQuotedResult());
    }

    @Test
    void testLargeString() {
        service.baz("largelargelargelargelargelargelargelarge");

        MethodTrack track = serviceTracker.getLastTrack();
        Assertions.assertEquals("largelargelargelargelargelarge...", track.getArguments()[0]);
    }

    @Test
    void testError() {
        try {
            service.err(11);
        } catch (RuntimeException e) {}

        MethodTrack track = serviceTracker.getLastTrack();
        Assertions.assertTrue(track.toString().contains("err(11) ERROR IllegalStateException: error"));
        Assertions.assertNotNull(track.getThrowable());
        Assertions.assertEquals("11", track.getArguments()[0]);
    }

    public static class Service {
        public String foo(int num) {
            return num + " call";
        }

        public String bar(int num) {
            return num + " bar";
        }

        public void baz(String in) {
        }

        public String err(int in) {
            throw new IllegalStateException("error");
        }
    }
}
