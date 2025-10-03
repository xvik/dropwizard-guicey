package ru.vyarus.dropwizard.guice.url.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.url.resource.support.DirectResource;

/**
 * @author Vyacheslav Rusakov
 * @since 02.10.2025
 */
public class RestPathUtilsTest {

    @Test
    void testPathBuilding() {

        Assertions.assertThat(RestPathUtils.getResourcePath(DirectResource.class))
                .isEqualTo("/direct");
        Assertions.assertThat(RestPathUtils.getResourcePath("some/%s", DirectResource.class, 11))
                .isEqualTo("/some/11/direct");

        Assertions.assertThat(RestPathUtils.buildPath(DirectResource.class)
                        .queryParam("q", 1).build())
                .isEqualTo("/direct?q=1");
        Assertions.assertThat(RestPathUtils.buildPath("some/%s", DirectResource.class, 11)
                        .queryParam("q", 1).build())
                .isEqualTo("/some/11/direct?q=1");

        Assertions.assertThat(RestPathUtils.buildSubResourcePath("some/%s/", DirectResource.class, 11)
                        .queryParam("q", 1).build())
                .isEqualTo("/some/11?q=1");
    }
}
