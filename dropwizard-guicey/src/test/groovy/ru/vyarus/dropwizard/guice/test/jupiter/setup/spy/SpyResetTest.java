package ru.vyarus.dropwizard.guice.test.jupiter.setup.spy;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean;

/**
 * @author Vyacheslav Rusakov
 * @since 19.02.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpyResetTest {

    @SpyBean
    Service spy;

    @Test
    void test1() {
        spy.foo();
        Mockito.verify(spy).foo();
    }

    @Test
    void test2() {
        spy.foo();
        // no second exec (put autoReset = false to make sure)
        Mockito.verify(spy).foo();
    }

    public static class Service {
        public String foo() {
            return "foo";
        }
    }
}
