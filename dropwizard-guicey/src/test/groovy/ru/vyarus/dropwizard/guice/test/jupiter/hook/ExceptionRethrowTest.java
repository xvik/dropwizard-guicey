package ru.vyarus.dropwizard.guice.test.jupiter.hook;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

import java.io.IOException;

/**
 * @author Vyacheslav Rusakov
 * @since 21.03.2025
 */
public class ExceptionRethrowTest extends AbstractPlatformTest {

    @Test
    void testCheckedException() {

        final Throwable ex = runFailed(Test1.class);
        Assertions.assertThat(ex).isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to run hook");
    }

    @Test
    void testRuntimeException() {

        final Throwable ex = runFailed(Test2.class);
        Assertions.assertThat(ex).isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test1 {
        @EnableHook
        static GuiceyConfigurationHook hook = builder -> {throw new IOException("test");};

        @Test
        void test() {

        }
    }

    @TestGuiceyApp(DefaultTestApp.class)
    @Disabled
    public static class Test2 {
        @EnableHook
        static GuiceyConfigurationHook hook = builder -> {throw new RuntimeException("test");};

        @Test
        void test() {

        }
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
