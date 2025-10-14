package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.rest.support.Resource1;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 18.09.2025
 */
@TestGuiceyApp(value = RestStubApp.class, useApacheClient = true)
public class MethodMatchTest {

    @StubRest(disableDropwizardExceptionMappers = true)
    RestClient rest;

    @Test
    void testMethodSearch() {
        final ResourceClient<Resource1> res1 = rest.subClient(Resource1.class);

        String res = res1.method(mock -> mock.get("test", null))
//                .pathParam("foo", "test")
                .as(String.class);
        Assertions.assertEquals("test", res);

        res = res1.method("get")
                .pathParam("foo", "test")
                .as(String.class);
        Assertions.assertEquals("test", res);
    }
}
