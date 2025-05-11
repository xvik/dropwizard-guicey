package ru.vyarus.dropwizard.guice.test.rest;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
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
public class IgnoreExtensionTest {

    @Test
    void testIgnoredExtension() throws Exception {
        final RestStubsHook rest = RestStubsHook.builder()
                .disableResources(Resource2.class)
                .disableJerseyExtensions(RestFilter2.class, RestExceptionMapper.class)
                .disableDropwizardExceptionMappers(true)
                .build();
        TestSupport.build(RestStubApp.class)
                .hooks(rest)
                .runCore(injector -> {
                    GuiceyConfigurationInfo info = injector.getInstance(GuiceyConfigurationInfo.class);

                    Assertions.assertEquals(new HashSet(Arrays.asList(
                                    Resource1.class,
                                    ErrorResource.class,
                                    RestFilter1.class,
                                    ManagedBean.class)),
                            new HashSet(info.getExtensions()));

                    Assertions.assertEquals(new HashSet(Arrays.asList(
                                    Resource2.class,
                                    RestFilter2.class,
                                    RestExceptionMapper.class,
                                    WebFilter.class)),
                            new HashSet(info.getExtensionsDisabled()));

                    // exception mapper not set
                    ProcessingException ex = Assertions.assertThrows(ProcessingException.class,
                            () -> rest.getRestClient().get("/error/foo", String.class));
                    Assertions.assertEquals("error", ex.getCause().getMessage());

                    return null;
                });

    }

    @Test
    void testIgnoreAllExtensions() throws Exception {
        final RestStubsHook rest = RestStubsHook.builder()
                .disableAllJerseyExtensions(true)
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
                                    ManagedBean.class)),
                            new HashSet(info.getExtensions()));

                    Assertions.assertEquals(new HashSet(Arrays.asList(
                                    RestFilter1.class,
                                    RestFilter2.class,
                                    RestExceptionMapper.class,
                                    WebFilter.class)),
                            new HashSet(info.getExtensionsDisabled()));

                    // exception mapper not set
                    ProcessingException ex = Assertions.assertThrows(ProcessingException.class,
                            () -> rest.getRestClient().get("/error/foo", String.class));
                    Assertions.assertEquals("error", ex.getCause().getMessage());

                    return null;
                });
    }

    @Test
    void testExactExtensions() throws Exception {
        final RestStubsHook rest = RestStubsHook.builder()
                .resources(Resource1.class, ErrorResource.class)
                .jerseyExtensions(RestFilter1.class)
                .disableDropwizardExceptionMappers(true)
                .build();
        TestSupport.build(RestStubApp.class)
                .hooks(rest)
                .runCore(injector -> {
                    GuiceyConfigurationInfo info = injector.getInstance(GuiceyConfigurationInfo.class);

                    Assertions.assertEquals(new HashSet(Arrays.asList(
                                    Resource1.class,
                                    ErrorResource.class,
                                    RestFilter1.class,
                                    ManagedBean.class)),
                            new HashSet(info.getExtensions()));

                    Assertions.assertEquals(new HashSet(Arrays.asList(
                                    Resource2.class,
                                    RestFilter2.class,
                                    RestExceptionMapper.class,
                                    WebFilter.class)),
                            new HashSet(info.getExtensionsDisabled()));

                    // exception mapper not set
                    ProcessingException ex = Assertions.assertThrows(ProcessingException.class,
                            () -> rest.getRestClient().get("/error/foo", String.class));
                    Assertions.assertEquals("error", ex.getCause().getMessage());

                    return null;
                });
    }

    @Test
    void testDisableDropwizardExtensions() throws Exception {
        final RestStubsHook rest = RestStubsHook.builder()
                .disableDropwizardExceptionMappers(true)
                .build();
        TestSupport.build(RestStubApp.class)
                .hooks(rest)
                .runCore(injector -> {
                    final WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class,
                            () -> rest.getRestClient().get("/error/1", String.class));
                    System.out.println(">>>ERROR:\n" + ex.getResponse().readEntity(String.class));

                    return null;
                });
    }
}
