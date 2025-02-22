package ru.vyarus.dropwizard.guice.test.general;

import io.dropwizard.lifecycle.Managed;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
public class BuilderRunCoreWithoutManaged {

    @Test
    void testCoreRun() throws Exception {

        UnusedManaged managed = TestSupport.build(App.class)
                .runCore(injector -> injector.getInstance(UnusedManaged.class));

        Assertions.assertTrue(managed.started);
        Assertions.assertTrue(managed.stopped);
    }

    @Test
    void testCoreRunWithoutManagedLifecycle() throws Exception {

        UnusedManaged managed = TestSupport.build(App.class)
                .runCoreWithoutManaged(injector -> injector.getInstance(UnusedManaged.class));

        Assertions.assertFalse(managed.started);
        Assertions.assertFalse(managed.stopped);
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(UnusedManaged.class)
                    .build();
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
