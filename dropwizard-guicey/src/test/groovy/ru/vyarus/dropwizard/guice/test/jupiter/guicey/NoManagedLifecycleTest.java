package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.general.BuilderRunCoreWithoutManaged;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
@TestGuiceyApp(value = BuilderRunCoreWithoutManaged.App.class, managedLifecycle = false)
public class NoManagedLifecycleTest {

    @Inject
    BuilderRunCoreWithoutManaged.UnusedManaged managed;

    @Test
    void testManagedNotStarted() {
        Assertions.assertFalse(managed.started);
    }
}
