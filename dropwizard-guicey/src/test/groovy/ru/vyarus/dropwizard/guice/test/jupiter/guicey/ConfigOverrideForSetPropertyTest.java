package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import io.dropwizard.configuration.ConfigurationParsingException;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.TestSupport;

import java.util.Set;

/**
 * @author Vyacheslav Rusakov
 * @since 04.03.2025
 */
public class ConfigOverrideForSetPropertyTest {

    @Test
    void testConfigOverrides() {

        ConfigurationParsingException ex = Assertions.assertThrows(ConfigurationParsingException.class, () -> {
            TestSupport.build(App.class)
                    // IMPOSSIBLE to specify with config override
                    .configOverride("foo", "['1', '2']")
                    .runCore();
        });

        Assertions.assertTrue(ex.getMessage()
                .contains("Failed to parse configuration at: foo; Cannot construct instance of `java.util.HashSet` (although at least one Creator exists)"));
    }

    public static class Config extends Configuration {

        private Set<String> foo;

        public Set<String> getFoo() {
            return foo;
        }
    }

    public static class App extends Application<Config> {
        @Override
        public void initialize(Bootstrap<Config> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Config configuration, Environment environment) throws Exception {
        }
    }
}
