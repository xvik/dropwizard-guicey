package ru.vyarus.dropwizard.guice.test.client.builder.track;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Vyacheslav Rusakov
 * @since 06.10.2025
 */
public class UriBuilderTrackTest {

    @Test
    void testUriBuilderTrack() throws Exception {

        RequestTracker tracker = new RequestTracker();
        final UriBuilder builder = tracker.track().getUriBuilder();
        builder.uri(URI.create("https://google.com"))
                .uri("http://google.com")
                .scheme("http")
                .schemeSpecificPart("/something")
                .userInfo("a:A")
                .host("localhost")
                .port(8080)
                .replacePath("/other")
                .path("api")
                .path(TrackRealTest.Resource.class)
                .path(TrackRealTest.Resource.class, "get")
                .path(TrackRealTest.Resource.class.getMethod("get"))
                .segment("segment", "segment2")
                .replaceMatrix("a=2;b=3;g")
                .matrixParam("c", 4)
                .replaceMatrixParam("c", 5)
                .replaceQuery("q1=1&q2=2")
                .queryParam("q2", 3)
                .replaceQueryParam("q3", 4)
                .fragment("fragment")
                .resolveTemplate("p1", "1")
                .resolveTemplate("p2", "2//3", true)
                .resolveTemplateFromEncoded("p3", "3")
                .resolveTemplates(ImmutableMap.of("p4", "4", "p5", "5"))
                .resolveTemplates(ImmutableMap.of("p6", "6//7"), true)
                .resolveTemplatesFromEncoded(ImmutableMap.of("p7", "7"));

        assertThat(tracker.getPaths()).containsOnly(
                "https://google.com",
                "http://google.com",
                "http",
                "/something",
                "a:A",
                "localhost",
                "8080",
                "/other",
                "api",
                "/root",
                "/",
                "/",
                "segment/segment2",
                "fragment");
        assertThat(tracker.getPathParams()).hasSize(7)
                .containsEntry("p1", "1")
                .containsEntry("p2", "2//3")
                .containsEntry("p3", "3")
                .containsEntry("p4", "4")
                .containsEntry("p5", "5")
                .containsEntry("p6", "6//7")
                .containsEntry("p7", "7");
        assertThat(tracker.getMatrixParams()).hasSize(4)
                .containsEntry("a", "2")
                .containsEntry("b", "3")
                .containsEntry("c", 5)
                .containsEntry("g", null);
        assertThat(tracker.getQueryParams()).hasSize(3)
                .containsEntry("q1", "1")
                .containsEntry("q2", 3)
                .containsEntry("q3", 4);

        assertThatThrownBy(builder::clone).isInstanceOf(UnsupportedOperationException.class);
        assertThat(builder.buildFromMap(ImmutableMap.of("1", "2"))).isNotNull();
        assertThat(builder.buildFromMap(ImmutableMap.of("1", "2"), true)).isNotNull();
        assertThat(builder.buildFromEncodedMap(ImmutableMap.of("1", "2"))).isNotNull();
        assertThat(builder.build("1", "2")).isNotNull();
        assertThat(builder.build(new Object[]{"1", "2"}, true)).isNotNull();
        assertThat(builder.buildFromEncoded("1", "2")).isNotNull();
        assertThat(builder.toTemplate()).isNotNull();
    }

}
