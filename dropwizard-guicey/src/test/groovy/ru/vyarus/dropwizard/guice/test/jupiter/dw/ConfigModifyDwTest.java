package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;

/**
 * @author Vyacheslav Rusakov
 * @since 05.03.2025
 */
public class ConfigModifyDwTest {

    @RegisterExtension
    static TestDropwizardAppExtension ext = TestDropwizardAppExtension.forApp(AutoScanApplication.class)
            .configOverrides("foo: 2", "bar: 3", "baa: 4")
            .configModifiers(config -> {
                config.foo = 12;
            })
            .debug()
            .create();


    @EnableSetup
    static TestEnvironmentSetup setup = ext ->
            ext.<TestConfiguration>configModifiers(config -> config.bar = 11);

    @Inject
    TestConfiguration configuration;

    @Test
    void testConfigModification() {
        Assertions.assertEquals(12, configuration.foo);
        Assertions.assertEquals(11, configuration.bar);
        Assertions.assertEquals(4, configuration.baa);
    }
}
