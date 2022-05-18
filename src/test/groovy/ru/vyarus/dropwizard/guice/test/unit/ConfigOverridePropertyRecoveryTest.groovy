package ru.vyarus.dropwizard.guice.test.unit


import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.spockframework.runtime.model.SpecInfo
import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import ru.vyarus.dropwizard.guice.test.GuiceyTestSupport
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideExtensionValue
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideValue
import ru.vyarus.spock.jupiter.engine.context.ClassContext

/**
 * @author Vyacheslav Rusakov
 * @since 18.05.2022
 */
class ConfigOverridePropertyRecoveryTest {

    @Test
    void testPropertyRecover() {
        // ConfigOverride implementations must restore original system property values
        System.setProperty("test.foo", "1")
        System.setProperty("test.bar", "2")


        def value1 = new ConfigOverrideValue("foo", () -> "11")
        value1.setPrefix("test")
        def value2 = new ConfigOverrideExtensionValue(ExtensionContext.Namespace.GLOBAL, "bar", "bar")
        value2.setPrefix("test")
        value2.resolveValue(new ClassContext(null, new SpecInfo()))

        GuiceyTestSupport support = new GuiceyTestSupport(AutoScanApplication.class, null, "test",
                value1, value2)

        support.before()

        Assertions.assertEquals("11", System.getProperty("test.foo"))
        Assertions.assertEquals("", System.getProperty("test.bar"))

        support.after()

        Assertions.assertEquals("1", System.getProperty("test.foo"))
        Assertions.assertEquals("2", System.getProperty("test.bar"))
    }
}
