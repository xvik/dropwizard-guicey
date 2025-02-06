package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * Note: injectOnce USELESS in this case
 *
 * @author Vyacheslav Rusakov
 * @since 06.02.2025
 */
@TestGuiceyApp(value = PerMethodInjectOnceGuiceyTest.App.class, injectOnce = true, debug = true)
@TestInstance(TestInstance.Lifecycle.PER_METHOD) // default
public class PerMethodInjectOnceGuiceyTest {

    @Inject
    Bean bean;

    static int testId;
    static int beanId;

    @Test
    @Order(1)
    void injectTest1() {
        Assertions.assertNotNull(bean);
        testId = System.identityHashCode(this);
        beanId = System.identityHashCode(bean);
    }

    @Test
    @Order(2)
    void injectTest2() {
        Assertions.assertNotNull(bean);
        // different test instance
        Assertions.assertNotEquals(testId, System.identityHashCode(this));
        // ofc, different bean
        Assertions.assertNotEquals(beanId, System.identityHashCode(bean));
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

    // prototype scope
    public static class Bean {}
}
