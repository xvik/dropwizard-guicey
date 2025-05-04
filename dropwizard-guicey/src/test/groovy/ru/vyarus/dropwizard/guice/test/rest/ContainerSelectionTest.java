package ru.vyarus.dropwizard.guice.test.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp;

/**
 * @author Vyacheslav Rusakov
 * @since 21.04.2025
 */
public class ContainerSelectionTest {

    @Test
    void testContainerSelection() throws Exception {
        final RestStubsHook rest = RestStubsHook.builder()
                .container(TestContainerPolicy.GRIZZLY)
                .build();

        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () ->
                TestSupport.build(RestStubApp.class)
                        .hooks(rest)
                        .runCore());

        Assertions.assertEquals("org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory is not available in classpath. " +
                "Add `org.glassfish.jersey.test-framework.providers:jersey-test-framework-provider-grizzly2` " +
                "dependency (version managed by dropwizard BOM)", ex.getMessage());
    }
}
