package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;

/**
 * @author Vyacheslav Rusakov
 * @since 05.03.2025
 */
public class ManualConfigDwTest {

    @RegisterExtension
    static TestDropwizardAppExtension ext = TestDropwizardAppExtension.forApp(AutoScanApplication.class)
            .config(() -> {
                TestConfiguration res = new TestConfiguration();
                res.baa = 33;
                return res;
            })
            .configModifiers(config -> config.foo = 11)
            .create();

    @Inject
    TestConfiguration config;

    @Test
    void testManualConfiguration() {
        Assertions.assertEquals(11, config.foo);
        Assertions.assertEquals(33, config.baa);
    }
}
