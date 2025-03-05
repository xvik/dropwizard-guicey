package ru.vyarus.dropwizard.guice.test.general;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import jakarta.inject.Singleton;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.util.RunResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
public class BuilderRunCoreWithoutManagedTest {

    @Test
    void testCoreRun() throws Exception {

        RunResult<Configuration> result = TestSupport.build(App.class).runCore();

        UnusedManaged managed = result.getBean(UnusedManaged.class);
        Assertions.assertTrue(managed.started);
        Assertions.assertTrue(managed.stopped);

        Assertions.assertEquals(Arrays.asList("lifeCycleStarting", "lifeCycleStarted", "lifeCycleStopping", "lifeCycleStopped"), result.<App>getApplication().events);
    }

    @Test
    void testCoreRunWithoutManagedLifecycle() throws Exception {

        RunResult<Configuration> result = TestSupport.build(App.class).runCoreWithoutManaged();

        UnusedManaged managed = result.getBean(UnusedManaged.class);
        Assertions.assertFalse(managed.started);
        Assertions.assertFalse(managed.stopped);

        Assertions.assertEquals(Arrays.asList("lifeCycleStarting", "lifeCycleStarted", "lifeCycleStopping", "lifeCycleStopped"), result.<App>getApplication().events);
    }

    public static class App extends DefaultTestApp {
        public final List<String> events = new ArrayList<>();

        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(UnusedManaged.class)
                    .build();
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.lifecycle().addEventListener(new LifeCycle.Listener() {
                @Override
                public void lifeCycleStarting(LifeCycle event) {
                    events.add("lifeCycleStarting");
                }

                @Override
                public void lifeCycleStarted(LifeCycle event) {
                    events.add("lifeCycleStarted");
                }

                @Override
                public void lifeCycleFailure(LifeCycle event, Throwable cause) {
                    events.add("lifeCycleFailure");
                }

                @Override
                public void lifeCycleStopping(LifeCycle event) {
                    events.add("lifeCycleStopping");
                }

                @Override
                public void lifeCycleStopped(LifeCycle event) {
                    events.add("lifeCycleStopped");
                }
            });
            // server listener is not called in any case (no reals server started)
            environment.lifecycle().addServerLifecycleListener(server -> events.add("serverStarted"));
        }
    }

    @Singleton
    public static class UnusedManaged implements Managed {
        public boolean started = false;
        public boolean stopped = false;

        @Override
        public void start() throws Exception {
            started = true;
        }

        @Override
        public void stop() throws Exception {
            stopped = true;
        }
    }
}
