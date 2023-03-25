package ru.vyarus.dropwizard.guice.test.jupiter;

import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;

import javax.inject.Inject;

/**
 * @author Vyacheslav Rusakov
 * @since 01.05.2020
 */
@TestGuiceyApp(AutoScanApplication.class)
public class NestedPropagationTest {

    @Inject
    Environment environment;

    @Test
    void checkInjection() {
        Assertions.assertNotNull(environment);
    }

    @Nested
    class Inner {

        @Inject
        Environment env; // intentionally different name

        @Test
        void checkInjection() {
            Assertions.assertNotNull(env);
        }
    }
}
