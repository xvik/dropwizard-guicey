package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

/**
 * @author Vyacheslav Rusakov
 * @since 05.03.2025
 */
public class ManualConfigErrorGuiceyTest extends AbstractPlatformTest {

    @AfterEach
    void tearDown() {
        SharedConfigurationState.clear();
        ConfigurationHooksSupport.reset();
    }

    @Test
    void testConfigPathUsed() {
        final Throwable ex = runFailed(Test1.class);
        Assertions.assertEquals("Configuration path can't be used with manual configuration instance: /some/path", ex.getMessage());
    }

    @Test
    void testConfigOverrideUsed() {
        final Throwable ex = runFailed(Test2.class);
        Assertions.assertEquals("Configuration overrides can't be used with manual configuration instance: [foo: 1]", ex.getMessage());
    }

    @Test
    void testConfigOverrideInstanceUsed() {
        final Throwable ex = runFailed(Test3.class);
        Assertions.assertEquals("Configuration overrides can't be used with manual configuration instance", ex.getMessage());
    }

    @Test
    void testConfigNull() {
        final Throwable ex = runFailed(Test4.class);
        Assertions.assertEquals("Configuration can't be null", ex.getMessage());
    }

    @Test
    void testConfigFail() {
        final Throwable ex = runFailed(Test5.class);
        Assertions.assertEquals("Manual configuration instance construction failed", ex.getMessage());
        Assertions.assertEquals("error", ex.getCause().getMessage());
    }


    @Test
    void testConfigDuplicate() {
        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
            TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                    .config(TestConfiguration::new)
                    .config(TestConfiguration::new);
        });
        Assertions.assertEquals("Manual configuration instance already set", ex.getMessage());
    }


    @Disabled
    public static class Test1 {

        @RegisterExtension
        static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                .config("/some/path")
                .config(TestConfiguration::new)
                .create();

        @Test
        void testConfigPathUsed() {
            Assertions.fail();
        }
    }

    @Disabled
    public static class Test2 {

        @RegisterExtension
        static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                .config(TestConfiguration::new)
                .configOverrides("foo: 1")
                .create();

        @Test
        void testConfigOverrideUsed() {
            Assertions.fail();
        }
    }

    @Disabled
    public static class Test3 {

        @RegisterExtension
        static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                .config(TestConfiguration::new)
                .configOverride("foo", () -> "1")
                .create();

        @Test
        void testConfigOverrideUsed() {
            Assertions.fail();
        }
    }

    @Disabled
    public static class Test4 {

        @RegisterExtension
        static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                .config(() -> null)
                .create();

        @Test
        void testConfigNull() {
            Assertions.fail();
        }
    }

    @Disabled
    public static class Test5 {

        @RegisterExtension
        static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                .config(() -> {throw new IllegalStateException("error");})
                .create();

        @Test
        void testConfigFail() {
            Assertions.fail();
        }
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
