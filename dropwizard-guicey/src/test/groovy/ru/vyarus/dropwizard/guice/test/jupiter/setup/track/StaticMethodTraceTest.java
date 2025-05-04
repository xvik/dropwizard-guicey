package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import com.google.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.track.Tracker;

/**
 * @author Vyacheslav Rusakov
 * @since 14.02.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
public class StaticMethodTraceTest {

    @Inject
    Service service;

    @TrackBean
    Tracker<Service> tracker;

    @Test
    void testStaticTrack() {

        service.istStatic();
        Base.intStatic();
        service.intDef();

        Assertions.assertThat(tracker.size()).isEqualTo(1);
        Assertions.assertThat(tracker.getLastTrack().toStringTrack()).isEqualTo("intDef()");
    }

    public interface Base {
        default void intDef() {}
        static void intStatic() {}
    }

    public static class Service implements Base {
        public static void istStatic() {
        }
    }
}
