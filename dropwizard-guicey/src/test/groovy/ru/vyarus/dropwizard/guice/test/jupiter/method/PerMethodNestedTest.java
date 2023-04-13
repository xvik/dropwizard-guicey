package ru.vyarus.dropwizard.guice.test.jupiter.method;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * @author Vyacheslav Rusakov
 * @since 04.06.2022
 */
public class PerMethodNestedTest {

    @RegisterExtension
    TestGuiceyAppExtension extension = TestGuiceyAppExtension.forApp(App.class)
            .create();

    @Inject
    Bean bean;

    @Test
    void checkInjection() {
        Assertions.assertEquals(0, bean.value);
        bean.value = 1;
    }

    @Nested
    class Inner {

        @Inject
        Bean bn; // intentionally different name

        @Test
        void test1() {
            Assertions.assertEquals(0, bn.value);
            bn.value = 2;
        }

        @Test
        void test2() {
            Assertions.assertEquals(0, bn.value);
            bn.value = 3;
        }
    }


    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Singleton
    public static class Bean {
        public int value = 0;
    }
}
