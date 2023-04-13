package ru.vyarus.dropwizard.guice.test.jupiter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * @author Vyacheslav Rusakov
 * @since 01.06.2020
 */
@TestGuiceyApp(AutoScanApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InjectionScopeTest {

    // new instance injected on each test
    @Inject
    TestBean bean;

    // the same context used for all tests (in class), so the same bean instance inserted before each test
    @Inject
    TestSingletonBean singletonBean;

    @Test
    @Order(1)
    public void testInjection() {
        bean.value = 5;
        singletonBean.value = 15;

        Assertions.assertEquals(5, bean.value);
        Assertions.assertEquals(15, singletonBean.value);

    }

    @Test
    @Order(2)
    public void testSharedState() {

        Assertions.assertEquals(0, bean.value);
        Assertions.assertEquals(15, singletonBean.value);
    }

    // bean is in prototype scope
    public static class TestBean {
        int value;
    }

    @Singleton
    public static class TestSingletonBean {
        int value;
    }
}
