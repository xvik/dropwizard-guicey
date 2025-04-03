package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

/**
 * @author Vyacheslav Rusakov
 * @since 05.03.2025
 */
public class WithBlockGuiceyTest {

    @RegisterExtension
    static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
            .with(extension -> {
                TestConfiguration conf = new TestConfiguration();
                conf.baa = 33;
                extension.config(() -> conf);

            })
            .create();

    @Inject
    TestConfiguration config;

    @Test
    void testManualConfiguration() {
        Assertions.assertEquals(33, config.baa);
    }
}
