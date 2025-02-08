package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 28.06.2024
 */
@TestGuiceyApp(PerClassPrototypeInjectionTest.App.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PerClassPrototypeInjectionTest {

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
        Assertions.assertEquals(testId, System.identityHashCode(this));
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
