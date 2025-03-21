package ru.vyarus.dropwizard.guice.bundles;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;

import java.io.IOException;

/**
 * @author Vyacheslav Rusakov
 * @since 21.03.2025
 */
public class InitExceptionTest {

    @Test
    void testCheckedException() {
        final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
            TestSupport.build(DefaultTestApp.class)
                    .hooks(builder -> builder.bundles(new GuiceyBundle() {
                        @Override
                        public void initialize(GuiceyBootstrap bootstrap) throws Exception {
                            throw new IOException("error");
                        }
                    }))
                    .runCore();
        });
        Assertions.assertEquals("Guicey bundle initialization failed", ex.getMessage());
    }

    @Test
    void testRuntimeException() {
        final RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> {
            TestSupport.build(DefaultTestApp.class)
                    .hooks(builder -> builder.bundles(new GuiceyBundle() {
                        @Override
                        public void initialize(GuiceyBootstrap bootstrap) throws Exception {
                            throw new RuntimeException("error");
                        }
                    }))
                    .runCore();
        });
        Assertions.assertEquals("error", ex.getMessage());
    }

}
