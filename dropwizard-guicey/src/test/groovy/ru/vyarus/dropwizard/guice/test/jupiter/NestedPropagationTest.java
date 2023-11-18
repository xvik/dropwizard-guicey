package ru.vyarus.dropwizard.guice.test.jupiter;

import io.dropwizard.setup.Environment;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;

import javax.inject.Inject;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.TestSupport;

/**
 * @author Vyacheslav Rusakov
 * @since 01.05.2020
 */
@TestGuiceyApp(AutoScanApplication.class)
public class NestedPropagationTest {

    @Inject
    Environment environment;

    @Test
    void checkInjection(DropwizardTestSupport support, ClientSupport client) {
        Assertions.assertNotNull(environment);
        Assertions.assertEquals(support, TestSupport.getContext());
        Assertions.assertEquals(client, TestSupport.getContextClient());
    }

    @Nested
    class Inner {

        @Inject
        Environment env; // intentionally different name

        @Test
        void checkInjection(DropwizardTestSupport support, ClientSupport client) {
            Assertions.assertNotNull(env);
            Assertions.assertEquals(support, TestSupport.getContext());
            Assertions.assertEquals(client, TestSupport.getContextClient());
        }
    }
}
