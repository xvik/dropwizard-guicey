package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import io.dropwizard.core.server.DefaultServerFactory;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;

/**
 * @author Vyacheslav Rusakov
 * @since 25.04.2025
 */
public class ManualConfigRestPathTest {

    @RegisterExtension
    static TestDropwizardAppExtension ext = TestDropwizardAppExtension.forApp(AutoScanApplication.class)
            .config(() -> {
                TestConfiguration res = new TestConfiguration();
                res.baa = 33;
                return res;
            })
            .restMapping("/foo")
            .create();

    @Inject
    TestConfiguration config;

    @Test
    void testRestMappingShortcutApplied() {

        Assertions.assertEquals("/foo/*",
                ((DefaultServerFactory)config.getServerFactory()).getJerseyRootPath().orElse(null));
    }
}
