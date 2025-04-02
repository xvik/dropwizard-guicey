package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.guicey.ObjectConfigOverrideGuiceyTest;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideValue;

/**
 * @author Vyacheslav Rusakov
 * @since 06.03.2021
 */
public class ObjectConfigOverrideDwTest {

    @RegisterExtension
    @Order(1)
    static ObjectConfigOverrideGuiceyTest.FooExtension ext = new ObjectConfigOverrideGuiceyTest.FooExtension();

    @RegisterExtension
    @Order(2)
    static TestDropwizardAppExtension app = TestDropwizardAppExtension.forApp(AutoScanApplication.class)
            .config("src/test/resources/ru/vyarus/dropwizard/guice/config.yml")
            .configOverrides("foo: 1")
            .configOverride("boo", "2")
            .configOverride("bar", () -> ext.getValue())
            .configOverrides(new ConfigOverrideValue("baa", () -> "44"))
            .create();

    @Inject
    TestConfiguration config;

    @BeforeAll
    public static void setUp() {
        // demonstrates that it is too late!
        ext.value = "33";
    }

    @Test
    void checkCorrectWiring() {
        Assertions.assertEquals(config.foo, 1);
        Assertions.assertEquals(config.bar, 22);
        Assertions.assertEquals(config.baa, 44);
        Assertions.assertEquals(config.boo, 2);
    }

    public static class FooExtension implements BeforeAllCallback {

        public String value = "11";
        private boolean init;

        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            value = "22";
            init = true;
        }

        public String getValue() {
            Preconditions.checkState(init, "Extension not recognized!");
            return value;
        }
    }
}
