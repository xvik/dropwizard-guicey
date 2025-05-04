package ru.vyarus.dropwizard.guice.test.track;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.track.stat.TrackerStats;

import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2025
 */
public class TrackerTest {

    @Test
    void testTracker() throws Exception {
        TrackersHook hook = new TrackersHook();
        final Tracker<Service> tracker = hook.track(Service.class)
                .trace(true)
                .add();
        TestSupport.build(DefaultTestApp.class)
                .hooks(hook)
                .runCore(injector -> {
                    Service service = injector.getInstance(Service.class);

                    // call service
                    Assertions.assertEquals("1 call", service.foo(1));

                    Assertions.assertEquals(Service.class, tracker.getType());
                    Assertions.assertEquals(1, tracker.size());
                    Assertions.assertEquals(1, tracker.getTracks().size());
                    Assertions.assertFalse(tracker.isEmpty());
                    MethodTrack track = tracker.getLastTrack();
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


                    Assertions.assertEquals(3, tracker.getTracks().size());
                    List<MethodTrack> tracks = tracker.getLastTracks(2);
                    Assertions.assertEquals("foo(2) = \"2 call\"", tracks.get(0).toStringTrack());
                    Assertions.assertEquals("bar(1) = \"1 bar\"", tracks.get(1).toStringTrack());


                    // search with mockito api
                    tracks = tracker.findTracks(mock -> when(
                            mock.foo(Mockito.anyInt()))
                    );
                    Assertions.assertEquals(2, tracks.size());

                    // few more calls (to check mocks correct reset)
                    Assertions.assertEquals("foo", tracks.get(0).getMethod().getName());
                    Assertions.assertEquals("foo", tracks.get(1).getMethod().getName());

                    tracks = tracker.findTracks(mock -> when(
                            mock.foo(Mockito.intThat(argument -> argument == 1)))
                    );
                    Assertions.assertEquals(1, tracks.size());
                    Assertions.assertEquals(1, tracks.get(0).getRawArguments()[0]);

                    // and another call to make sure results not cached
                    Assertions.assertEquals("1 call", service.foo(1));

                    tracks = tracker.findTracks(mock -> when(
                            mock.foo(Mockito.intThat(argument -> argument == 1)))
                    );
                    Assertions.assertEquals(2, tracks.size());


                    service.baz("small");

                    track = tracker.getLastTrack();
                    Assertions.assertTrue(track.toString().contains("baz(\"small\")"));
                    Assertions.assertNull(track.getResult());
                    Assertions.assertNull(track.getRawResult());
                    Assertions.assertNull(track.getQuotedResult());


                    service.baz("largelargelargelargelargelargelargelarge");
                    track = tracker.getLastTrack();
                    Assertions.assertEquals("largelargelargelargelargelarge...", track.getArguments()[0]);


                    Assertions.assertThrows(IllegalStateException.class, () -> service.err(11));
                    track = tracker.getLastTrack();
                    Assertions.assertTrue(track.toString().contains("err(11) ERROR IllegalStateException: error"));
                    Assertions.assertNotNull(track.getThrowable());
                    Assertions.assertEquals("11", track.getArguments()[0]);


                    final TrackerStats stats = tracker.getStats();
                    Assertions.assertNotNull(stats);
                    Assertions.assertEquals(4, stats.getMethods().size());
                    Assertions.assertNotNull(stats.render());


                    tracker.clear();
                    Assertions.assertEquals(0, tracker.size());

                    return null;
                });
    }

    @Test
    void resetTrackersTest() throws Exception {
        TrackersHook hook = new TrackersHook();
        final Tracker<Service> tracker = hook.track(Service.class)
                .trace(true)
                .add();
        TestSupport.build(DefaultTestApp.class)
                .hooks(hook)
                .runCore(injector -> {
                    Service service = injector.getInstance(Service.class);

                    // call service
                    Assertions.assertEquals("1 call", service.foo(1));
                    Assertions.assertEquals(1, tracker.size());

                    hook.resetTrackers();
                    Assertions.assertEquals(0, tracker.size());

                    Assertions.assertEquals(tracker, hook.getTracker(Service.class));

                    return null;
                });
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
