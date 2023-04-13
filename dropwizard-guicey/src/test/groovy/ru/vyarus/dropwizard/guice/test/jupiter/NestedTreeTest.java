package ru.vyarus.dropwizard.guice.test.jupiter;

import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;

import jakarta.inject.Inject;

/**
 * @author Vyacheslav Rusakov
 * @since 01.05.2020
 */
public class NestedTreeTest {

    @TestGuiceyApp(AutoScanApplication.class)
    @Nested
    class Level1 {

        @Inject
        Environment environment;

        @Test
        void checkExtensionApplied() {
            Assertions.assertNotNull(environment);
        }

        @Nested
        class Level2 {
            @Inject
            Environment env;

            @Test
            void checkExtensionApplied() {
                Assertions.assertNotNull(env);
            }

            @Nested
            class Level3 {

                @Inject
                Environment envr;

                @Test
                void checkExtensionApplied() {
                    Assertions.assertNotNull(envr);
                }
            }
        }
    }

    @Nested
    class NotAffected {
        @Inject
        Environment environment;

        @Test
        void extensionNotApplied() {
            Assertions.assertNull(environment);
        }
    }
}
