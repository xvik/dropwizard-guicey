package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import com.google.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.Tracker;

/**
 * @author Vyacheslav Rusakov
 * @since 17.02.2025
 */
public class InstanceTrackDetectionTest extends AbstractTrackerTest{

    @Test
    void testInstanceTrackingDetection() {

        Throwable th = runFailed(Test1.class);
        Assertions.assertThat(th.getMessage()).isEqualTo(
                "Incorrect @TrackBean 'r.v.d.g.t.j.s.t.InstanceTrackDetectionTest$Test1.tracker' declaration: target " +
                        "bean 'Service' bound by instance and so can't be tracked");
    }

    @TestGuiceyApp(Test1.App.class)
    @Disabled
    public static class Test1 {
        @Inject
        Service service;

        @TrackBean(trace = true)
        Tracker<Service> tracker;

        @Test
        void testInstanceTracking() {
            // nothing
        }

        public static class App extends DefaultTestApp {

            @Override
            protected GuiceBundle configure() {
                return GuiceBundle.builder()
                        .modules(builder -> builder.bind(Service.class).toInstance(new Service()))
                        .build();
            }
        }

        public static class Service  {

            public String foo(int i) {
                return "foo" + i;
            }
        }
    }
}
