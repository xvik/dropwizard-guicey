package ru.vyarus.dropwizard.guice.debug.provision;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.BindingAnnotation;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Vyacheslav Rusakov
 * @since 25.03.2025
 */
public class WrongConfigUsageTest extends AbstractPlatformTest {

    @Test
    void testWrongUsageDetection() {
        String out = run(Test1.class);
        Assertions.assertThat(out).contains("Possible mistakes (unqualified JIT bindings):\n" +
                "\n" +
                "\t\t @Inject Sub:\n" +
                "\t\t\t  instance             [@Singleton]     @Config(\"val2\") Sub                                                                   : 111 ms \t\t ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:129)\n" +
                "\t\t\t  instance             [@Singleton]     @Marker Sub                                                                           : 111 ms \t\t ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindCustomQualifiers(ConfigBindingModule.java:87)\n" +
                "\t\t\t> JIT                  [@Prototype]     Sub                                                                                   : 111 ms \t\t \n" +
                "\n" +
                "\t\t @Inject Uniq:\n" +
                "\t\t\t  instance             [@Singleton]     @Config Uniq                                                                          : 111 ms \t\t ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:117)\n" +
                "\t\t\t> JIT                  [@Prototype]     Uniq                                                                                  : 111 ms");
    }

    @TestGuiceyApp(App.class)
    @Disabled
    public static class Test1 {

        @Test
        void test() {
        }
    }

    public static class App extends Application<Config> {

        @Override
        public void initialize(Bootstrap<Config> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(Service.class)
                    .printGuiceProvisionTime()
                    .build());
        }

        @Override
        public void run(Config configuration, Environment environment) throws Exception {
        }
    }

    public static class Config extends Configuration {
        // not unique but one with custom annotation
        @Marker
        private Sub val = new Sub();

        private Sub val2 = new Sub();

        private Uniq uniq = new Uniq();

        public Sub getVal() {
            return val;
        }

        public Sub getVal2() {
            return val2;
        }

        public Uniq getUniq() {
            return uniq;
        }
    }

    public static class Sub {
        @JsonProperty
        String val;
    }

    public static class Uniq {
        @JsonProperty
        String val;
    }

    @Retention(RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @BindingAnnotation
    public @interface Marker {}

    @EagerSingleton
    public static class Service {
        @Inject
        Sub val;
        @Inject
        Uniq uniq;
    }

    @Override
    protected String clean(String out) {
        return unifyMs(out);
    }
}
