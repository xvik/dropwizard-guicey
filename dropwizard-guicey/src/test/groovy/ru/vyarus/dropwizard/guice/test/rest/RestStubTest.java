package ru.vyarus.dropwizard.guice.test.rest;

import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.test.TestSupport;
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
 * @since 21.04.2025
 */
public class RestStubTest {

    @Test
    void testSimpleRun() throws Exception {
        final RestStubsRunner rest = RestStubsRunner.builder()
                .disableDropwizardExceptionMappers(true)
                .build();
        TestSupport.build(RestStubApp.class)
                .hooks(rest)
                .runCore(injector -> {
                    GuiceyConfigurationInfo info = injector.getInstance(GuiceyConfigurationInfo.class);

                    Assertions.assertEquals(new HashSet(Arrays.asList(
                                    Resource1.class,
                                    Resource2.class,
                                    ErrorResource.class,
                                    RestFilter1.class,
                                    RestFilter2.class,
                                    ManagedBean.class,
                                    RestExceptionMapper.class)),
                            new HashSet(info.getExtensions()));

                    // web extension auto disabled
                    Assertions.assertTrue(info.getExtensionsDisabled().contains(WebFilter.class));

                    ManagedBean managed = injector.getInstance(ManagedBean.class);
                    // managed called once
                    Assertions.assertEquals(1, managed.beforeCnt);
                    Assertions.assertEquals(0, managed.afterCnt);


                    String res = rest.getRestClient().get("/1/foo", String.class);
                    Assertions.assertEquals("foo", res);

                    // rest filter used
                    RestFilter1 filter = injector.getInstance(RestFilter1.class);
                    Assertions.assertTrue(filter.called);


                    // test error
                    WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class,
                            () -> rest.getRestClient().get("/error/foo", String.class));
                    RestExceptionMapper exceptionMapper = injector.getInstance(RestExceptionMapper.class);
                    Assertions.assertTrue(exceptionMapper.called);
                    Assertions.assertEquals("error", ex.getResponse().readEntity(String.class));

                    return null;
                });

    }
}
