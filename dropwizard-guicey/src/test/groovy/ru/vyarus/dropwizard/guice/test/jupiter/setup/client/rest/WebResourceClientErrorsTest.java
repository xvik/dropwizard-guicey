package ru.vyarus.dropwizard.guice.test.jupiter.setup.client.rest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.client.TestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.rest.WebResourceClient;

/**
 * @author Vyacheslav Rusakov
 * @since 17.10.2025
 */
public class WebResourceClientErrorsTest extends AbstractPlatformTest {

    @Test
    void testNoRestStubsError() {
        Assertions.assertThat(runFailed(Test1.class).getMessage())
                .isEqualTo("Resource client can't be used under lightweight guicey test without @StubRest: WebResourceClientErrorsTest$Test1.rest");
    }

    @Test
    void testBadResource() {
        Assertions.assertThat(runFailed(Test2.class).getMessage())
                .isEqualTo("Target resource class must be specified as generic (ResourceClient<RestClass>) in field: WebResourceClientErrorsTest$Test2.rest");
    }

    @Test
    void testBadType() {
        Assertions.assertThat(runFailed(Test3.class).getMessage())
                .isEqualTo("Field WebResourceClientErrorsTest$Test3.rest annotated with @WebResourceClient, but its type is not ResourceClient");
    }

    @TestGuiceyApp(RestApp.class)
    @Disabled
    public static class Test1 {

        @WebResourceClient
        ResourceClient<RestApp.Resource> rest;

        @Test
        void test() {

        }
    }


    @TestGuiceyApp(RestApp.class)
    @Disabled
    public static class Test2 {

        @WebResourceClient
        ResourceClient<?> rest;

        @Test
        void test() {

        }
    }

    @TestGuiceyApp(RestApp.class)
    @Disabled
    public static class Test3 {

        @WebResourceClient
        TestClient<?> rest;

        @Test
        void test() {

        }
    }

    @Override
    protected String clean(String out) {
        return out;
    }
}
