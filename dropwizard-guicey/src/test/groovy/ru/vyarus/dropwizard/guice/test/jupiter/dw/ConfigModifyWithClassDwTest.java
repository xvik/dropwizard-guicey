package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.server.DefaultServerFactory;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.util.ConfigModifier;

/**
 * @author Vyacheslav Rusakov
 * @since 05.03.2025
 */
@TestDropwizardApp(value = AutoScanApplication.class, configModifiers = ConfigModifyWithClassDwTest.FooModifier.class,
        configOverride = {"foo: 2", "bar: 3", "baa: 4"})
public class ConfigModifyWithClassDwTest {

    @EnableSetup
    static TestEnvironmentSetup setup = ext -> ext.configModifiers(BarModifier.class, GenericModifier.class);

    @Inject
    TestConfiguration configuration;

    @Test
    void testConfigModification() {
        Assertions.assertEquals(11, configuration.foo);
        Assertions.assertEquals(12, configuration.bar);
        Assertions.assertEquals(4, configuration.baa);
        Assertions.assertEquals(22, ((DefaultServerFactory) configuration.getServerFactory()).getAdminMaxThreads());
    }

    public static class FooModifier implements ConfigModifier<TestConfiguration> {
        @Override
        public void modify(TestConfiguration config) throws Exception {
            config.foo = 11;
        }
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
            ((DefaultServerFactory)config.getServerFactory()).setAdminMaxThreads(22);;
        }
    }
}
