package ru.vyarus.dropwizard.guice.test.jupiter;

import io.dropwizard.setup.Environment;
import io.dropwizard.testing.DropwizardTestSupport;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.util.support.TestSupportHolder;

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
            void checkExtensionApplied(DropwizardTestSupport support) {
                Assertions.assertNotNull(env);
                Assertions.assertEquals(support, TestSupport.getContext());
            }

            @Nested
            class Level3 {

                @Inject
                Environment envr;

                @Test
                void checkExtensionApplied(DropwizardTestSupport support) {
                    Assertions.assertNotNull(envr);
                    Assertions.assertEquals(support, TestSupport.getContext());
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
            Assertions.assertFalse(TestSupportHolder.isContextSet());
        }
    }
}
