package ru.vyarus.dropwizard.guice.test.general;

import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.TestSupport;

/**
 * @author Vyacheslav Rusakov
 * @since 18.11.2023
 */
public class CustomPrefixTest {
    @Test
    void testDefaultPrefix() throws Exception {

        DropwizardTestSupport<TestConfiguration> support = TestSupport.build(AutoScanApplication.class)
                .configOverride("foo: 11")
                .restMapping("rest")
                .buildCore();

        try {
            // apply properties
            support.before();

            Assertions.assertEquals("11", System.getProperty("dw.foo"));
            Assertions.assertEquals("/rest/*", System.getProperty("dw.server.rootPath"));
        } finally {
            support.after();
        }
    }


    @Test
    void testPrefixApplied() throws Exception {

        DropwizardTestSupport<TestConfiguration> support = TestSupport.build(AutoScanApplication.class)
                .propertyPrefix("ttt")
                .configOverride("foo: 11")
                .restMapping("rest")
                .buildCore();

        try {
            // apply properties
            support.before();

            Assertions.assertEquals("11", System.getProperty("ttt.foo"));
            Assertions.assertEquals("/rest/*", System.getProperty("ttt.server.rootPath"));
        } finally {
            support.after();
        }
    }
}
