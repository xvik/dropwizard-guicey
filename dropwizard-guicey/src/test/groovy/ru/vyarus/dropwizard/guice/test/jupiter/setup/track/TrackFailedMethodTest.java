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
public class TrackFailedMethodTest {

    @Inject
    Service service;

    @TrackBean
    Tracker<Service> tracker;

    @Test
    void testNoRowObjects() {
        Assertions.assertThat(tracker).isNotNull();
        try {
            service.foo();
            Assertions.fail("Should have thrown exception");
        } catch (Exception ex) {
        }

        MethodTrack track = tracker.getLastTrack();
        Assertions.assertThat(track.getArguments()).isNotNull();
        Assertions.assertThat(track.getRawArguments()).isNotNull();
        Assertions.assertThat(track.getResult()).isNull();
        Assertions.assertThat(track.getRawResult()).isNull();
        Assertions.assertThat(track.getQuotedResult()).isNull();
        Assertions.assertThat(track.getThrowable()).isNotNull();
        Assertions.assertThat(track.getThrowable().getMessage()).isEqualTo("Failed");
        Assertions.assertThat(track.toStringTrack()).isEqualTo("foo() ERROR IllegalStateException: Failed");

    }

    public static class Service {
        public void foo() {
            throw new IllegalStateException("Failed");
        }
    }
}
