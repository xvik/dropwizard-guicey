package ru.vyarus.dropwizard.guice.test.jupiter.setup;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;

/**
 * @author Vyacheslav Rusakov
 * @since 05.03.2025
 */
@TestGuiceyApp(AutoScanApplication.class)
public class ManualConfigTest {

    @EnableSetup
    static TestEnvironmentSetup setup = ext -> ext.config(() -> {
        TestConfiguration conf = new TestConfiguration();
        conf.foo = 12;
        return conf;
    });

    @Inject
    TestConfiguration config;

    @Test
    void testManualConfigFromSetup() {
        Assertions.assertEquals(12, config.foo);
    }
}
