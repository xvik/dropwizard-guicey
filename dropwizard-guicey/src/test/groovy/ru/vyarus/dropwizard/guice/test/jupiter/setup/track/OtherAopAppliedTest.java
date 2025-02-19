package ru.vyarus.dropwizard.guice.test.jupiter.setup.track;

import com.google.inject.Inject;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.Tracker;

/**
 * @author Vyacheslav Rusakov
 * @since 14.02.2025
 */
@TestGuiceyApp(OtherAopAppliedTest.App.class)
public class OtherAopAppliedTest {

    @Inject
    Service service;

    @TrackBean
    Tracker<Service> tracker;

    @Test
    void testCustomAopCounted() {
        Assertions.assertThat(tracker).isNotNull();

        // call service, intercepted with aop
        Assertions.assertThat(service.foo()).isEqualTo("foo!CUSTOM!");
        // aop part must be counted
        Assertions.assertThat(tracker.getLastTrack().getRawResult()).isEqualTo("foo!CUSTOM!");
    }

    public static class App extends DefaultTestApp {

        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .modules(binder -> {
                        binder.bindInterceptor(Matchers.only(Service.class), Matchers.any(), new CustomInterceptor());
                    })
                    .build();
        }
    }

    public static class CustomInterceptor implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            return invocation.proceed() + "!CUSTOM!";
        }
    }

    public static class Service {
        public String foo() {
            return "foo";
        }
    }
}
