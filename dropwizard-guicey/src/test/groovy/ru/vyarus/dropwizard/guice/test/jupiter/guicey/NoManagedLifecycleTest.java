package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.general.BuilderRunCoreWithoutManagedTest;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
@TestGuiceyApp(value = BuilderRunCoreWithoutManagedTest.App.class, managedLifecycle = false)
public class NoManagedLifecycleTest {

    @Inject
    BuilderRunCoreWithoutManagedTest.UnusedManaged managed;

    @Test
    void testManagedNotStarted() {
        Assertions.assertFalse(managed.started);
    }
}
