package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest;

import com.google.inject.Inject;
import javax.ws.rs.ProcessingException;
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
 * @since 22.02.2025
 */
@TestGuiceyApp(RestStubApp.class)
public class IgnoreAllJerseyExtTest {

    @StubRest(disableAllJerseyExtensions = true, disableDropwizardExceptionMappers = true)
    RestClient rest;

    @Inject
    GuiceyConfigurationInfo info;

    @Test
    void testExtensionsDisabled() {
        Assertions.assertNotNull(rest);

        // extensions enabled
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
    }

    @Test
    void testExceptionMapperNotSet() {

        ProcessingException ex = Assertions.assertThrows(ProcessingException.class,
                () -> rest.get("/error/foo", String.class));
        Assertions.assertEquals("error", ex.getCause().getMessage());
    }
}
