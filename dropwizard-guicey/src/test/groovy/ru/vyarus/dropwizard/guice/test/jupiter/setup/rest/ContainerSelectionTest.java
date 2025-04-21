package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.TestContainerPolicy;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 26.02.2025
 */
public class ContainerSelectionTest extends AbstractPlatformTest {

    @Test
    void testContainerSelection() {
        final Throwable ex = runFailed(Test1.class);
        Assertions.assertEquals("org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory is not available in classpath. " +
                "Add `org.glassfish.jersey.test-framework.providers:jersey-test-framework-provider-grizzly2` " +
                "dependency (version managed by dropwizard BOM)", ex.getMessage());
    }

    @TestGuiceyApp(RestStubApp.class)
    @Disabled
    public static class Test1 {

        @StubRest(container = TestContainerPolicy.GRIZZLY)
        RestClient rest;

        @Test
        void test() {

        }
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
