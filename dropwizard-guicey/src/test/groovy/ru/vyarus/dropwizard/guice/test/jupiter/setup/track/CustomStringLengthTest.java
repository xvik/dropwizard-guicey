package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import com.google.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.track.MethodTrack;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.track.Tracker;

/**
 * @author Vyacheslav Rusakov
 * @since 14.02.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
public class CustomStringLengthTest {

    @Inject
    Service service;

    @TrackBean(maxStringLength = 5)
    Tracker<Service> tracker;

    @Test
    void testNoRowObjects() {
        Assertions.assertThat(tracker).isNotNull();
        service.foo("boobooboobooboo");

        MethodTrack track = tracker.getLastTrack();
        Assertions.assertThat(track.getArguments()).isEqualTo(new String[]{"boobo..."});
        Assertions.assertThat(track.getResult()).isEqualTo("foofo...");
        Assertions.assertThat(track.getRawArguments()).isEqualTo(new Object[]{"boobooboobooboo"});
        Assertions.assertThat(track.getRawResult()).isEqualTo("foofoofoofoofoofoofooboobooboobooboo");
        Assertions.assertThat(track.toStringTrack()).isEqualTo("foo(\"boobo...\") = \"foofo...\"");

    }

    public static class Service {
        public String foo(String in) {
            return "foofoofoofoofoofoofoo" + in;
        }
    }
}
