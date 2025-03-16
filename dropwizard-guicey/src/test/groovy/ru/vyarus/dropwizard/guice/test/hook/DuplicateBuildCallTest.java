package ru.vyarus.dropwizard.guice.test.hook;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;

/**
 * @author Vyacheslav Rusakov
 * @since 14.03.2025
 */
public class DuplicateBuildCallTest {

    @AfterEach
    void tearDown() {
        ConfigurationHooksSupport.reset();
    }

    @Test
    void testDuplicateBuildCall() {
        ((GuiceyConfigurationHook) GuiceBundle.Builder::build).register();
        final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> new DefaultTestApp().run("server"));
        org.assertj.core.api.Assertions.assertThat(ex.getMessage())
                .isEqualTo(".build() was already called for guice bundle. Most likely, it was called second time in GuiceyConfigurationHook");
    }
}
