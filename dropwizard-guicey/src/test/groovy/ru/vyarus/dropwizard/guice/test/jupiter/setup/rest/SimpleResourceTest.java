package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import com.google.inject.Inject;
import javax.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.support.ErrorResource;
import ru.vyarus.dropwizard.guice.test.rest.support.ManagedBean;
import ru.vyarus.dropwizard.guice.test.rest.support.Resource1;
import ru.vyarus.dropwizard.guice.test.rest.support.Resource2;
import ru.vyarus.dropwizard.guice.test.rest.support.RestExceptionMapper;
import ru.vyarus.dropwizard.guice.test.rest.support.RestFilter1;
import ru.vyarus.dropwizard.guice.test.rest.support.RestFilter2;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;
import ru.vyarus.dropwizard.guice.test.rest.support.WebFilter;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Vyacheslav Rusakov
 * @since 20.02.2025
 */
@TestGuiceyApp(value = RestStubApp.class, debug = true)
public class SimpleResourceTest {

    @StubRest(disableDropwizardExceptionMappers = true)
    RestClient rest;

    @Inject
    GuiceyConfigurationInfo info;

    @Inject
    RestFilter1 filter;

    @Inject
    ManagedBean managed;

    @Inject
    RestExceptionMapper exceptionMapper;

    @Test
    void testRestStub() {
        Assertions.assertNotNull(rest);

        // extensions enabled
        Assertions.assertEquals(new HashSet<>(Arrays.asList(
                        Resource1.class,
                        Resource2.class,
                        ErrorResource.class,
                        RestFilter1.class,
                        RestFilter2.class,
                        ManagedBean.class,
                        RestExceptionMapper.class)),
                new HashSet<>(info.getExtensions()));

        // web extension auto disabled
        Assertions.assertTrue(info.getExtensionsDisabled().contains(WebFilter.class));

        // managed called once
        Assertions.assertEquals(1, managed.beforeCnt);
        Assertions.assertEquals(0, managed.afterCnt);

        String res = rest.get("/1/foo", String.class);
        Assertions.assertEquals("foo", res);

        // rest filter used
        Assertions.assertTrue(filter.called);
    }

    @Test
    void testError() {

        WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class,
                () -> rest.get("/error/foo", String.class));
        Assertions.assertTrue(exceptionMapper.called);
        Assertions.assertEquals("error", ex.getResponse().readEntity(String.class));
    }

}
