package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 01.05.2020
 */
public class ConfigOverrideGuiceyTest {

    interface ConfigOverrideCheck {

        TestConfiguration get();

        @Test
        default void configurationOverridden() {
            Assertions.assertEquals(get().foo, 2);
            Assertions.assertEquals(get().bar, 12);
            Assertions.assertEquals(get().baa, 4);
        }
    }

    @TestGuiceyApp(value = AutoScanApplication.class,
            config = "src/test/resources/ru/vyarus/dropwizard/guice/config.yml",
            configOverride = {"foo=2", "bar=12"})
    @Nested
    class ConfigOverrideTest implements ConfigOverrideCheck {

        @Inject
        TestConfiguration configuration;

        @Override
        public TestConfiguration get() {
            return configuration;
        }
    }

    @TestGuiceyApp(value = AutoScanApplication.class,
            configOverride = {"foo=2", "bar=12", "baa=4"})
    @Nested
    class EmptyConfigOverrideTest implements ConfigOverrideCheck {

        @Inject
        TestConfiguration configuration;

        @Override
        public TestConfiguration get() {
            return configuration;
        }
    }
}
