package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.test.general.BuilderRunCoreWithoutManagedTest;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
public class NoManagedLifecycleManualTest {

    @RegisterExtension
    static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(BuilderRunCoreWithoutManagedTest.App.class)
            .disableManagedLifecycle()
            .create();

    @Inject
    BuilderRunCoreWithoutManagedTest.UnusedManaged managed;

    @Test
    void testManagedNotStarted() {
        Assertions.assertFalse(managed.started);
    }
}
