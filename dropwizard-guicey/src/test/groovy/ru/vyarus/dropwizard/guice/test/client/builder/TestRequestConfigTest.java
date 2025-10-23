package ru.vyarus.dropwizard.guice.test.client.builder;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.client.builder.track.RequestTracker;
import ru.vyarus.dropwizard.guice.test.client.builder.track.impl.mock.TargetMock;
import ru.vyarus.dropwizard.guice.test.client.util.SourceAwareValue;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 03.10.2025
 */
public class TestRequestConfigTest {

    final NewCookie cookie = new NewCookie("Test", "tst");

    final Function<WebTarget, WebTarget> pathFunc = target -> target.path("foo");
    final Consumer<Invocation.Builder> reqFunc = req -> req.header("foo", "bar");
    final Object extension = new Object();
    final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    @Test
    void testEmptyConfig() {
        // WHEN empty config
        final TestRequestConfig config = new TestRequestConfig(null);
        assertEmptyConfig(config);
    }

    @Test
    void testConfiguration() {
        // WHEN filled config
        final TestRequestConfig config = new TestRequestConfig(null)
                .configurePath(pathFunc)
                .configureRequest(reqFunc)
                .property("prop", "true")
                .register(Integer.class)
                .register(extension)
                .accept("text/plain")
                .acceptLanguage("EN")
                .acceptEncoding("gzip")
                .cacheControl("max-age=604800, must-revalidate")
                .queryParam("q1", "1")
                .pathParam("p1", "2")
                .matrixParam("m1", "1")
                .header("Header", "value")
                .cookie(cookie)
                .formDateFormatter(formatter)
                .formDateTimeFormatter(DateTimeFormatter.ISO_DATE)
                .debug(true);

        assertFullConfig(config);

        // WHEN config inherit values
        TestRequestConfig next = new TestRequestConfig(config);
        assertFullConfig(next);

        // WHEN config reset
        config.clear();
        assertEmptyConfig(config);

        // check no exception applying modifications (no way to asset correctness)
        final RequestTracker tracker = new RequestTracker();
        next.applyRequestConfiguration(tracker.track());

        assertThat(tracker.getPaths()).hasSize(1).contains("foo");
        assertThat(tracker.getProperties()).hasSize(1).containsEntry("prop", "true");
        assertThat(tracker.getExtensions()).hasSize(2)
                .containsEntry(Integer.class, Integer.class)
                .containsEntry(Object.class, extension);
        assertThat(tracker.getCacheHeader()).isEqualTo("must-revalidate, max-age=604800");
        assertThat(tracker.getAcceptHeader()).containsOnly("text/plain");
        assertThat(tracker.getLanguageHeader()).containsOnly("EN");
        assertThat(tracker.getEncodingHeader()).containsOnly("gzip");
        assertThat(tracker.getQueryParams()).hasSize(1).containsEntry("q1", "1");
        assertThat(tracker.getPathParams()).hasSize(1).containsEntry("p1", "2");
        assertThat(tracker.getMatrixParams()).hasSize(1).containsEntry("m1", "1");
        assertThat(tracker.getHeaders()).hasSize(2)
                .containsEntry("Header", "value")
                .containsEntry("foo", "bar");
        assertThat(tracker.getCookies()).hasSize(1).containsEntry("Test", cookie);

        assertThat(tracker.getLog()).isEqualTo("\n" +
                "\tResolve template                          at r.v.d.g.t.c.builder.(TestRequestConfig.java:867)\n" +
                "\t\t(encodeSlashInPath=false encoded=true)\n" +
                "\t\tp1=2\n" +
                "\n" +
                "\tQuery param                               at r.v.d.g.t.c.b.u.conf.(JerseyRequestConfigurer.java:82)\n" +
                "\t\tq1=1\n" +
                "\n" +
                "\tMatrix param                              at r.v.d.g.t.c.b.u.conf.(JerseyRequestConfigurer.java:82)\n" +
                "\t\tm1=1\n" +
                "\n" +
                "\tProperty                                  at r.v.d.g.t.c.builder.(TestRequestConfig.java:877)\n" +
                "\t\tprop=true\n" +
                "\n" +
                "\tRegister                                  at r.v.d.g.t.c.b.u.conf.(JerseyRequestConfigurer.java:59)\n" +
                "\t\tInteger                      (java.lang)                \n" +
                "\n" +
                "\tRegister                                  at r.v.d.g.t.c.b.u.conf.(JerseyRequestConfigurer.java:61)\n" +
                "\t\tObject                       (java.lang)                \n" +
                "\n" +
                "\tPath                                      at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:30)\n" +
                "\t\tfoo\n" +
                "\n" +
                "\tAccept                                    at r.v.d.g.t.c.builder.(TestRequestConfig.java:897)\n" +
                "\t\t[text/plain]\n" +
                "\n" +
                "\tAccept Language                           at r.v.d.g.t.c.builder.(TestRequestConfig.java:900)\n" +
                "\t\t[EN]\n" +
                "\n" +
                "\tAccept Encoding                           at r.v.d.g.t.c.builder.(TestRequestConfig.java:903)\n" +
                "\t\t[gzip]\n" +
                "\n" +
                "\tHeader                                    at r.v.d.g.t.c.builder.(TestRequestConfig.java:906)\n" +
                "\t\tHeader=value\n" +
                "\n" +
                "\tCookie                                    at r.v.d.g.t.c.builder.(TestRequestConfig.java:909)\n" +
                "\t\t$Version=1;Test=tst\n" +
                "\n" +
                "\tCache                                     at r.v.d.g.t.c.builder.(TestRequestConfig.java:912)\n" +
                "\t\tmust-revalidate, max-age=604800\n" +
                "\n" +
                "\tHeader                                    at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:31)\n" +
                "\t\tfoo=bar\n\n");
    }

    @Test
    void testFlagsCorrectness() {
        final TestRequestConfig config = new TestRequestConfig(null);
        config.configurePath(pathFunc);
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredPathModifiers()).hasSize(1).element(0).isEqualTo(pathFunc);
        assertThat(config.getConfiguredPathModifiersSource()).hasSize(1)
                .element(0).extracting(SourceAwareValue::get).isEqualTo(pathFunc);

        config.clear().configureRequest(reqFunc);
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredRequestModifiers()).hasSize(1).element(0).isEqualTo(reqFunc);
        assertThat(config.getConfiguredRequestModifiersSource()).hasSize(1)
                .element(0).extracting(SourceAwareValue::get).isEqualTo(reqFunc);

        config.clear().property("prop", "true");
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredProperties()).hasSize(1).containsOnly("prop");
        assertThat(config.getConfiguredPropertiesMap()).hasSize(1).containsEntry("prop", "true");
        assertThat(config.getConfiguredPropertiesSource()).hasSize(1).containsKey("prop");

        config.clear().register(Integer.class);
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredExtensions()).hasSize(1).containsOnly(Integer.class);
        assertThat(config.getConfiguredExtensionsMap()).hasSize(1).containsEntry(Integer.class, Integer.class);
        assertThat(config.getConfiguredExtensionsSource()).hasSize(1).containsKey(Integer.class);

        config.clear().accept("text/plain");
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredAccepts()).hasSize(1).element(0).isEqualTo("text/plain");
        assertThat(config.getConfiguredAcceptsSource().get()).isEqualTo(new String[]{"text/plain"});

        config.clear().acceptLanguage("EN");
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredLanguages()).hasSize(1).element(0).isEqualTo("EN");
        assertThat(config.getConfiguredLanguagesSource().get()).isEqualTo(new String[]{"EN"});

        config.clear().acceptEncoding("gzip");
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredEncodings()).hasSize(1).element(0).isEqualTo("gzip");
        assertThat(config.getConfiguredEncodingsSource().get()).isEqualTo(new String[]{"gzip"});

        config.clear().cacheControl("max-age=604800, must-revalidate");
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredCacheControl().toString()).isEqualTo("must-revalidate, max-age=604800");
        assertThat(config.getConfiguredCacheControlSource().get().toString()).isEqualTo("must-revalidate, max-age=604800");

        config.clear().queryParam("q1", "1");
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredQueryParams()).hasSize(1).containsOnly("q1");
        assertThat(config.getConfiguredQueryParamsMap()).hasSize(1).containsEntry("q1", "1");
        assertThat(config.getConfiguredQueryParamsSource()).hasSize(1).containsKey("q1");

        config.clear().pathParam("p1", "2");
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredPathParams()).hasSize(1).containsOnly("p1");
        assertThat(config.getConfiguredPathParamsMap()).hasSize(1).containsEntry("p1", "2");
        assertThat(config.getConfiguredPathParamsSource()).hasSize(1).containsKey("p1");

        config.clear().header("Header", "value");
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredHeaders()).hasSize(1).containsOnly("Header");
        assertThat(config.getConfiguredHeadersMap()).hasSize(1).containsEntry("Header", "value");
        assertThat(config.getConfiguredHeadersSource()).hasSize(1).containsKey("Header");

        config.clear().cookie(cookie);
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredCookies()).hasSize(1).containsOnly("Test");
        assertThat(config.getConfiguredCookiesMap()).hasSize(1).containsEntry("Test", cookie);
        assertThat(config.getConfiguredCookiesSource()).hasSize(1).containsKey("Test");

        config.clear().formDateFormatter(formatter);
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredFormDateFormatter()).isEqualTo(formatter);
        assertThat(config.getConfiguredFormDateFormatterSource())
                .extracting(SourceAwareValue::get).isEqualTo(formatter);

        config.clear().formDateTimeFormatter(DateTimeFormatter.ISO_DATE);
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.getConfiguredFormDateTimeFormatter()).isEqualTo(DateTimeFormatter.ISO_DATE);
        assertThat(config.getConfiguredFormDateTimeFormatterSource())
                .extracting(SourceAwareValue::get).isEqualTo(DateTimeFormatter.ISO_DATE);


        assertThat(config.getConfiguredFormDateTimeFormatterSource().toString())
                .isEqualTo("Value from at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:217)");

        // debug is not a part of defaults (but also copied)
        config.clear().debug(true);
        assertThat(config.hasConfiguration()).isFalse();
        assertThat(config.isDebugEnabled()).isTrue();
    }

    @Test
    void testRequestAssertion() {
        final TestRequestConfig config = new TestRequestConfig(null);
        final List<String> assertCalled = new ArrayList<>();
        config
                .header("Header", "value")
                .assertRequest(tracker -> assertCalled.add("called"));

        assertThat(config.isDebugEnabled()).isTrue();

        config.applyRequestConfiguration(new TargetMock()).buildGet();
        assertThat(assertCalled).containsExactly("called");
    }

    private void assertEmptyConfig(TestRequestConfig config) {
        assertThat(config.hasConfiguration()).isFalse();
        assertThat(config.printConfiguration()).isEqualTo("\n" +
                "\tNo configurations\n");

        assertThat(config.getConfiguredPathModifiers()).isEmpty();
        assertThat(config.getConfiguredRequestModifiers()).isEmpty();

        assertThat(config.getConfiguredProperties()).isEmpty();
        assertThat(config.getConfiguredPropertiesMap()).isEmpty();

        assertThat(config.getConfiguredExtensions()).isEmpty();
        assertThat(config.getConfiguredExtensionsMap()).isEmpty();

        assertThat(config.getConfiguredCacheControl()).isNull();

        assertThat(config.getConfiguredAccepts()).isEmpty();
        assertThat(config.getConfiguredLanguages()).isEmpty();
        assertThat(config.getConfiguredEncodings()).isEmpty();

        assertThat(config.getConfiguredQueryParams()).isEmpty();
        assertThat(config.getConfiguredQueryParamsMap()).isEmpty();

        assertThat(config.getConfiguredMatrixParams()).isEmpty();
        assertThat(config.getConfiguredMatrixParamsMap()).isEmpty();

        assertThat(config.getConfiguredPathParams()).isEmpty();
        assertThat(config.getConfiguredPathParamsMap()).isEmpty();

        assertThat(config.getConfiguredHeaders()).isEmpty();
        assertThat(config.getConfiguredHeadersMap()).isEmpty();

        assertThat(config.getConfiguredCookies()).isEmpty();
        assertThat(config.getConfiguredCookiesMap()).isEmpty();

        assertThat(config.getConfiguredFormDateFormatter()).isNull();
        assertThat(config.getConfiguredFormDateTimeFormatter()).isNull();

        assertThat(config.isDebugEnabled()).isFalse();
    }

    private void assertFullConfig(TestRequestConfig config) {
        config.printConfiguration();
        assertThat(config.hasConfiguration()).isTrue();
        assertThat(config.printConfiguration()).isEqualTo("\n" +
                "\tPath params:\n" +
                "\t\tp1=2                                      at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:56)\n" +
                "\n" +
                "\tQuery params:\n" +
                "\t\tq1=1                                      at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:55)\n" +
                "\n" +
                "\tMatrix params:\n" +
                "\t\tm1=1                                      at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:57)\n" +
                "\n" +
                "\tHeaders:\n" +
                "\t\tHeader=value                              at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:58)\n" +
                "\n" +
                "\tCookies:\n" +
                "\t\tTest=tst;Version=1                        at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:59)\n" +
                "\n" +
                "\tProperties:\n" +
                "\t\tprop=true                                 at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:48)\n" +
                "\n" +
                "\tExtensions:\n" +
                "\t\tInteger                                   at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:49)\n" +
                "\t\tObject                                    at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:50)\n" +
                "\n" +
                "\tAccept:\n" +
                "\t\ttext/plain                                at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:51)\n" +
                "\n" +
                "\tLanguage:\n" +
                "\t\tEN                                        at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:52)\n" +
                "\n" +
                "\tEncoding:\n" +
                "\t\tgzip                                      at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:53)\n" +
                "\n" +
                "\tPath modifiers:\n" +
                "\t\t<lambda>                                  at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:46)\n" +
                "\n" +
                "\tRequest modifiers:\n" +
                "\t\t<lambda>                                  at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:47)\n" +
                "\n" +
                "\tCache:\n" +
                "\t\tmust-revalidate, max-age=604800           at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:54)\n" +
                "\n" +
                "\tCustom Date (java.util) formatter:\n" +
                "\t\tSimpleDateFormat                          at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:60)\n" +
                "\n" +
                "\tCustom Date (java.time) formatter:\n" +
                "\t\tDateTimeFormatter                         at r.v.d.g.t.c.builder.(TestRequestConfigTest.java:61)\n");

        assertThat(config.getConfiguredPathModifiers()).hasSize(1).element(0).isEqualTo(pathFunc);
        assertThat(config.getConfiguredRequestModifiers()).hasSize(1).element(0).isEqualTo(reqFunc);

        assertThat(config.getConfiguredProperties()).containsOnly("prop");
        assertThat(config.getConfiguredPropertiesMap()).hasSize(1).containsEntry("prop", "true");

        assertThat(config.getConfiguredExtensions()).containsOnly(Integer.class, Object.class);
        assertThat(config.getConfiguredExtensionsMap()).hasSize(2)
                .containsEntry(Integer.class, Integer.class)
                .containsEntry(Object.class, extension);

        assertThat(RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class).toString(config.getConfiguredCacheControl()))
                .isEqualTo("must-revalidate, max-age=604800");

        assertThat(config.getConfiguredAccepts()).containsOnly("text/plain");
        assertThat(config.getConfiguredLanguages()).containsOnly("EN");
        assertThat(config.getConfiguredEncodings()).containsOnly("gzip");

        assertThat(config.getConfiguredQueryParams()).containsOnly("q1");
        assertThat(config.getConfiguredQueryParamsMap()).hasSize(1).containsEntry("q1", "1");

        assertThat(config.getConfiguredPathParams()).containsOnly("p1");
        assertThat(config.getConfiguredPathParamsMap()).hasSize(1).containsEntry("p1", "2");

        assertThat(config.getConfiguredHeaders()).containsOnly("Header");
        assertThat(config.getConfiguredHeadersMap()).hasSize(1).containsEntry("Header", "value");

        assertThat(config.getConfiguredCookies()).containsOnly("Test");
        assertThat(config.getConfiguredCookiesMap()).hasSize(1).containsEntry("Test", cookie);

        assertThat(config.getConfiguredFormDateFormatter()).isEqualTo(formatter);
        assertThat(config.getConfiguredFormDateTimeFormatter()).isEqualTo(DateTimeFormatter.ISO_DATE);

        assertThat(config.isDebugEnabled()).isTrue();
    }
}
