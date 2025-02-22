package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.test.general.BuilderRunCoreWithoutManaged;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
public class NoManagedLifecycleManualTest {

    @RegisterExtension
    static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(BuilderRunCoreWithoutManaged.App.class)
            .disableManagedLifecycle()
            .create();

    @Inject
    BuilderRunCoreWithoutManaged.UnusedManaged managed;

    @Test
    void testManagedNotStarted() {
        Assertions.assertFalse(managed.started);
    }
}
