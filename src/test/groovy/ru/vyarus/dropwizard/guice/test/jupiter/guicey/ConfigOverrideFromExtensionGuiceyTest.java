package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

import javax.inject.Inject;

/**
 * @author Vyacheslav Rusakov
 * @since 10.05.2022
 */
@ExtendWith({ConfigOverrideFromExtensionGuiceyTest.ConfigExtension.class,
        ConfigOverrideFromExtensionGuiceyTest.ConfigExtension2.class})
public class ConfigOverrideFromExtensionGuiceyTest {

    @RegisterExtension
    static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(App.class)
            .configOverrides("foo:1")
            .configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "ext0")
            .configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "ext1")
            .configOverrideByExtension(ExtensionContext.Namespace.create("sample"), "key", "ext2")
            .create();

    @Inject
    Config config;

    @Test
    void testConfigOverrides() {

        Assertions.assertEquals(1, config.getFoo());
        Assertions.assertEquals(10, config.getExt1());
        Assertions.assertEquals(20, config.getExt2());
        Assertions.assertNull(config.getExt0());
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

    public static class Config extends Configuration {
        private Integer foo;
        private Integer ext1;
        private Integer ext2;
        private Integer ext0;

        @JsonProperty
        public Integer getFoo() {
            return foo;
        }

        @JsonProperty
        public Integer getExt1() {
            return ext1;
        }

        @JsonProperty
        public Integer getExt2() {
            return ext2;
        }

        @JsonProperty
        public Integer getExt0() {
            return ext0;
        }
    }

    public static class ConfigExtension implements BeforeAllCallback {

        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            context.getStore(ExtensionContext.Namespace.GLOBAL).put("ext1", 10);
        }
    }

    public static class ConfigExtension2 implements BeforeAllCallback {

        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            context.getStore(ExtensionContext.Namespace.create("sample")).put("key", 20);
        }
    }
}
