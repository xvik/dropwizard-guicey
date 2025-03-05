package ru.vyarus.dropwizard.guice.test.general;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.server.DefaultServerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.util.ConfigModifier;

/**
 * @author Vyacheslav Rusakov
 * @since 05.03.2025
 */
public class BuilderConfigModifyTest {

    @Test
    void testConfigModificationsApplied() throws Exception {
        TestConfiguration conf = TestSupport.build(AutoScanApplication.class)
                .config("src/test/resources/ru/vyarus/dropwizard/guice/config.yml")
                .configOverrides("foo: 2", "bar: 3", "baa: 4")
                .configModifiers(config -> config.foo = 11)
                .configModifiers(BarModifier.class, GenericModifier.class)
                .runCore(injector -> injector.getInstance(TestConfiguration.class));

        Assertions.assertEquals(11, conf.foo);
        Assertions.assertEquals(12, conf.bar);
        Assertions.assertEquals(4, conf.baa);
        Assertions.assertEquals(22, ((DefaultServerFactory) conf.getServerFactory()).getAdminMaxThreads());
    }

    @Test
    void testConfigModificationAppliedForInstance() throws Exception {
        TestConfiguration conf = TestSupport.build(AutoScanApplication.class)
                .config(new TestConfiguration())
                .configModifiers(config -> config.foo = 11)
                .configModifiers(BarModifier.class, GenericModifier.class)
                .runCore(injector -> injector.getInstance(TestConfiguration.class));

        Assertions.assertEquals(11, conf.foo);
        Assertions.assertEquals(12, conf.bar);
        Assertions.assertEquals(0, conf.baa);
        Assertions.assertEquals(22, ((DefaultServerFactory)conf.getServerFactory()).getAdminMaxThreads());
    }

    @Test
    void testConfigModifyError() {
        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
             TestSupport.build(AutoScanApplication.class)
                     .config(new TestConfiguration())
                     .configModifiers(config -> {throw new IllegalArgumentException("error");})
                     .runCore();
             
         });

        Assertions.assertTrue(ex.getMessage().startsWith("Configuration modification failed for ru.vyarus.dropwizard.guice.test.general.BuilderConfigModifyTest"));
        Assertions.assertEquals("error", ex.getCause().getMessage());
    }

    public static class BarModifier implements ConfigModifier<TestConfiguration> {
        @Override
        public void modify(TestConfiguration config) throws Exception {
            config.bar = 12;
        }
    }

    public static class GenericModifier implements ConfigModifier<Configuration> {
        @Override
        public void modify(Configuration config) throws Exception {
            ((DefaultServerFactory) config.getServerFactory()).setAdminMaxThreads(22);
            ;
        }
    }
}
