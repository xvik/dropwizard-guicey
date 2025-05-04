package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import com.google.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.track.Tracker;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;

/**
 * @author Vyacheslav Rusakov
 * @since 14.02.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
public class SpyCompatibilityTest {

    @SpyBean
    // this is spy (internal to AOP interceptor)
    Service spy;

    @Inject
    // this is a AOPed class (with both aop handlers applied)
    Service realService;

    @TrackBean
    Tracker<Service> tracker;

    @Test
    void testSpyCompatibility() {
        Assertions.assertThat(spy).isNotNull().isNotEqualTo(realService);
        
        realService.foo();

        // tracked spy instance call, not guice bean
        Assertions.assertThat(tracker.getLastTrack().getInstanceHash()).isEqualTo(PrintUtils.identity(spy));
    }

    @Test
    void testCallingMethodsOnSpy() {
        spy.foo();
        Assertions.assertThat(tracker.size()).isEqualTo(1);
        Assertions.assertThat(tracker.getLastTrack().getInstanceHash()).isEqualTo(PrintUtils.identity(spy));

        // spy actually called just once
        Mockito.verify(spy, Mockito.times(1)).foo();
    }

    public static class Service {

        public String foo() {
            return "foo";
        }
    }
}
