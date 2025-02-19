package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import com.google.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.MethodTrack;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.Tracker;

/**
 * @author Vyacheslav Rusakov
 * @since 14.02.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
public class NoRowObjectsTest {

    @Inject
    Service service;

    @TrackBean(keepRawObjects = false)
    Tracker<Service> tracker;

    @Test
    void testNoRowObjects() {
        Assertions.assertThat(tracker).isNotNull();
        service.foo(1);

        MethodTrack track = tracker.getLastTrack();
        Assertions.assertThat(track.getArguments()).isEqualTo(new String[]{"1"});
        Assertions.assertThat(track.getResult()).isEqualTo("foo1");
        Assertions.assertThat(track.getRawArguments()).isNull();
        Assertions.assertThat(track.getRawResult()).isNull();

    }

    public static class Service {
        public String foo(int i) {
            return "foo" + i;
        }
    }
}
