package ru.vyarus.dropwizard.guice.test.client.builder;

import com.google.common.collect.ImmutableMap;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.eclipse.jetty.http.HttpHeader;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.message.internal.TracingLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.builder.TestSupportHolder;
import ru.vyarus.dropwizard.guice.test.client.builder.track.impl.mock.TargetMock;
import ru.vyarus.dropwizard.guice.test.client.builder.util.VoidBodyReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 08.10.2025
 */
public class RequestBuilderTest {

    final Function<WebTarget, WebTarget> pathFunc = target -> target.path("foo");
    final Consumer<Invocation.Builder> reqFunc = req -> req.header("foo", "bar");
    final Object extension = new Object();
    final CacheControl cacheControl = RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class)
            .fromString("max-age=604800, must-revalidate");


    @BeforeEach
    void setUp() {
        TestSupportHolder.reset();
    }

    @Test
    void testRequestBuilder() {
        List<String> called = new ArrayList<>();
        TestClientRequestBuilder builder = new TestClientRequestBuilder(new TargetMock(), "GET", null, null)
                .configurePath(pathFunc)
                .configureRequest(reqFunc)
                .notFollowRedirects()
                .noBodyMappingForVoid()
                .accept("application/json")
                .acceptLanguage("en")
                .acceptEncoding("gzip")
                .queryParam("q1", "1")
                .queryParams(ImmutableMap.of("q2", 2, "q3", 3))
                .matrixParam("m1", "1")
                .matrixParams(ImmutableMap.of("m2", 2, "m3", 3))
                .pathParam("p1", 1)
                .pathParams(ImmutableMap.of("p2", 2, "p3", 3))
                .header(HttpHeader.ALT_SVC, "11")
                .header("A", "a")
                .headers(ImmutableMap.of("B", "b", "C", 12))
                .cookie("c1", "1")
                .cookie(new NewCookie("c2", "11"))
                .cookies(ImmutableMap.of("c3", "3", "c4", "4"))
                .property("prop1", "foo")
                .properties(ImmutableMap.of("prop2", "bar", "prop3", "baz"))
                .register(Integer.class)
                .register(extension)
                .cacheControl("max-age=604800, must-revalidate")
                .enableJerseyTrace()
                .assertRequest(tracker ->
                        called.add("called"));

        TestRequestConfig config = builder.getConfig();
        assertThat(config.getConfiguredPathModifiers()).hasSize(1).element(0).isEqualTo(pathFunc);
        assertThat(config.getConfiguredRequestModifiers()).hasSize(1).element(0).isEqualTo(reqFunc);
        assertThat(config.getConfiguredAccepts()).hasSize(1).containsOnly("application/json");
        assertThat(config.getConfiguredLanguages()).hasSize(1).containsOnly("en");
        assertThat(config.getConfiguredEncodings()).hasSize(1).containsOnly("gzip");

        assertThat(config.getConfiguredQueryParamsMap()).hasSize(3)
                .containsEntry("q1", "1")
                .containsEntry("q2", 2)
                .containsEntry("q3", 3);

        assertThat(config.getConfiguredMatrixParamsMap()).hasSize(3)
                .containsEntry("m1", "1")
                .containsEntry("m2", 2)
                .containsEntry("m3", 3);

        assertThat(config.getConfiguredPathParamsMap()).hasSize(3)
                .containsEntry("p1", 1)
                .containsEntry("p2", 2)
                .containsEntry("p3", 3);

        assertThat(config.getConfiguredHeadersMap()).hasSize(6)
                .containsEntry("A", "a")
                .containsEntry("B", "b")
                .containsEntry("C", 12)
                .containsEntry(HttpHeader.ALT_SVC.asString(), "11")
                .containsEntry(TracingLogger.HEADER_ACCEPT, "true")
                .containsEntry(TracingLogger.HEADER_THRESHOLD, TracingLogger.Level.SUMMARY.name());

        assertThat(config.getConfiguredCookiesMap()).hasSize(4)
                .containsEntry("c1", new NewCookie("c1", "1"))
                .containsEntry("c2", new NewCookie("c2", "11"))
                .containsEntry("c3", new NewCookie("c3", "3"))
                .containsEntry("c4", new NewCookie("c4", "4"));

        assertThat(config.getConfiguredPropertiesMap()).hasSize(4)
                .containsEntry("prop1", "foo")
                .containsEntry("prop2", "bar")
                .containsEntry("prop3", "baz")
                .containsEntry(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);

        assertThat(config.getConfiguredExtensionsMap()).hasSize(3)
                .containsEntry(Integer.class, Integer.class)
                .containsEntry(Object.class, extension)
                .containsEntry(VoidBodyReader.class, VoidBodyReader.class);

        assertThat(config.getConfiguredCacheControl()).isEqualTo(cacheControl);
        assertThat(config.isDebugEnabled()).isTrue();

        builder.invoke();
        assertThat(called).hasSize(1).containsOnly("called");
    }

    @Test
    void testCustomMethods() {
        TestClientRequestBuilder builder = new TestClientRequestBuilder(new TargetMock(), "GET", null, null)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .acceptLanguage(Locale.CANADA)
                .cacheControl(cacheControl)
                .debug();

        TestRequestConfig config = builder.getConfig();
        assertThat(config.getConfiguredAccepts()).hasSize(1).containsOnly("application/json");
        assertThat(config.getConfiguredLanguages()).hasSize(1).containsOnly(Locale.CANADA.toString());
        assertThat(config.getConfiguredCacheControl()).isEqualTo(cacheControl);
        assertThat(config.isDebugEnabled()).isTrue();
    }

    @Test
    void testResponseTypeMappings() {
        List<String> calls = new ArrayList<>();
        new TestClientRequestBuilder(new TargetMock(), "GET", null, null)
                .assertRequest(tracker -> {
                    calls.add("1");
                    assertThat(tracker.getResultMappingClass()).isEqualTo(Void.class);
                })
                .asVoid();

        new TestClientRequestBuilder(new TargetMock(), "GET", null, null)
                .assertRequest(tracker -> {
                    calls.add("2");
                    assertThat(tracker.getResultMappingClass()).isEqualTo(Integer.class);
                })
                .as(Integer.class);

        new TestClientRequestBuilder(new TargetMock(), "GET", null, null)
                .assertRequest(tracker -> {
                    calls.add("3");
                    assertThat(tracker.getResultMappingString()).isEqualTo("List<Integer>");
                })
                .as(new GenericType<List<Integer>>() {});

        new TestClientRequestBuilder(new TargetMock(), "GET", null, null)
                .assertRequest(tracker -> {
                    calls.add("4");
                    assertThat(tracker.getResultMapping()).isNull();
                })
                .invoke();

        assertThat(calls).containsExactly("1", "2", "3", "4");
    }

    @Test
    void testToString() {
        assertThat(new TestClientRequestBuilder(new TargetMock(), "GET", null, null).toString())
                .isEqualTo("Request builder: GET ");
    }
}
