package ru.vyarus.dropwizard.guice.test.jupiter.setup;

import javax.inject.Inject;
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
public class WithBlockTest {

    @EnableSetup
    static TestEnvironmentSetup setup = ext -> ext
            .with(extension -> {
                // just an example! for config better to use #config directly
                TestConfiguration conf = new TestConfiguration();
                conf.foo = 12;
                extension.config(() -> conf);
            });

    @Inject
    TestConfiguration cfg;

    @Test
    void testWithBlock() {
        Assertions.assertEquals(12, cfg.foo);
    }
}
