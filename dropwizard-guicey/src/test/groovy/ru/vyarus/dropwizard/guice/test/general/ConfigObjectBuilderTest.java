package ru.vyarus.dropwizard.guice.test.general;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.util.RunResult;

/**
 * @author Vyacheslav Rusakov
 * @since 17.11.2023
 */
public class ConfigObjectBuilderTest {

    @Test
    void testCoreRunWithConfigObject() throws Exception {
        TestConfiguration config = new TestConfiguration();
        config.foo = 2;
        config.bar = 12;

        RunResult<TestConfiguration> res = TestSupport.build(AutoScanApplication.class)
                .config(config).runCore();
        Assertions.assertEquals(res.getConfiguration(), config);
        Assertions.assertEquals(res.getConfiguration().foo, 2);
        Assertions.assertEquals(res.getConfiguration().bar, 12);
    }

    @Test
    void testWebRunWithConfigObject() throws Exception {
        TestConfiguration config = new TestConfiguration();
        config.foo = 2;
        config.bar = 12;

        RunResult<TestConfiguration> support = TestSupport.build(AutoScanApplication.class)
                .config(config).runWeb();
        Assertions.assertEquals(support.getConfiguration(), config);
        Assertions.assertEquals(support.getConfiguration().foo, 2);
        Assertions.assertEquals(support.getConfiguration().bar, 12);
    }

    @Test
    void testConfigObjectWithConfigPath() {
        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () ->
                TestSupport.build(AutoScanApplication.class)
                        .config(new TestConfiguration())
                        .config("/path/to/config")
                        .runCore());

        ex.printStackTrace();
        Assertions.assertEquals("Configuration object can't be used together with yaml configuration",  ex.getMessage());


        ex = Assertions.assertThrows(IllegalStateException.class, () ->
                TestSupport.build(AutoScanApplication.class)
                        .config(new TestConfiguration())
                        .config("/path/to/config")
                        .runWeb());

        ex.printStackTrace();
        Assertions.assertEquals("Configuration object can't be used together with yaml configuration",  ex.getMessage());
    }

    @Test
    void testConfigObjectWithOverride() {
        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () ->
                TestSupport.build(AutoScanApplication.class)
                        .config(new TestConfiguration())
                        .configOverride("foo", "12")
                        .runCore());

        ex.printStackTrace();
        Assertions.assertEquals("Configuration object can't be used together with yaml configuration",  ex.getMessage());
    }

    @Test
    void testConfigObjectWithConfigProvider() {
        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () ->
                TestSupport.build(AutoScanApplication.class)
                        .config(new TestConfiguration())
                        .configSourceProvider(new ResourceConfigurationSourceProvider())
                        .runCore());

        ex.printStackTrace();
        Assertions.assertEquals("Configuration object can't be used together with yaml configuration",  ex.getMessage());
    }
}
