package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

/**
 * @author Vyacheslav Rusakov
 * @since 04.03.2025
 */
public class ConfigModifyGuiceyTest {

    @RegisterExtension
    static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
            .configOverrides("foo: 2", "bar: 3", "baa: 4")
            .configModifiers(config -> {
                config.foo = 12;
            })
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
