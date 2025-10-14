package ru.vyarus.dropwizard.guice.test.client;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.client.builder.TestRequestConfig;
import ru.vyarus.dropwizard.guice.test.client.support.ClientApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 07.10.2025
 */
@TestGuiceyApp(value = ClientApp.class, useApacheClient = true)
public class ClientDefaultsTest {

    final Function<WebTarget, WebTarget> pathFunc = target -> target.path("foo");
    final Consumer<Invocation.Builder> reqFunc = req -> req.header("foo", "bar");
    final Object extension = new Object();
    final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    final CacheControl cacheControl = RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class)
            .fromString("max-age=604800, must-revalidate");

    @Test
    void testDefaults(ClientSupport support) {
        support.defaultHeader("A", "a")
                .defaultHeader(HttpHeader.ACCESS_CONTROL_MAX_AGE, "12")
                .defaultHeader("B", () -> 11)
                .defaultHeader(HttpHeader.ACCESS_CONTROL_EXPOSE_HEADERS, () -> 13)
                .defaultQueryParam("q", 1)
                .defaultQueryParam("q2", () -> 2)
                .defaultMatrixParam("m", 1)
                .defaultMatrixParam("m2", () -> 2)
                .defaultPathParam("p", 1)
                .defaultPathParam("p2", () -> 2)
                .defaultProperty("PR", 1)
                .defaultProperty("PR2", () -> 2)
                .defaultRegister(Integer.class)
                .defaultRegister(extension)
                .defaultRegister(String.class, () -> "12")
                .defaultFormDateFormatter(formatter)
                .defaultFormDateTimeFormatter(DateTimeFormatter.ISO_DATE)
                .defaultCookie("c1", "1")
                .defaultCookie(new NewCookie.Builder("c2").value("11").build())
                .defaultCookie("c3", () -> new NewCookie.Builder("c3").value("12").build())
                .defaultAccept("application/json")
                .defaultLanguage("en")
                .defaultEncoding("gzip")
                .defaultCacheControl("max-age=604800, must-revalidate")
                .defaultDebug(true)
                .defaultPathConfiguration(pathFunc)
                .defaultRequestConfiguration(reqFunc);

        support.printDefaults();
        final TestRequestConfig defaults = support.getDefaults();
        assertThat(defaults.getConfiguredHeadersMap()).hasSize(4)
                .containsEntry("A", "a")
                .containsEntry("B", 11)
                .containsEntry(HttpHeader.ACCESS_CONTROL_MAX_AGE.asString(), "12")
                .containsEntry(HttpHeader.ACCESS_CONTROL_EXPOSE_HEADERS.asString(), 13);

        assertThat(defaults.getConfiguredQueryParamsMap()).hasSize(2)
                .containsEntry("q", 1)
                .containsEntry("q2", 2);

        assertThat(defaults.getConfiguredMatrixParamsMap()).hasSize(2)
                .containsEntry("m", 1)
                .containsEntry("m2", 2);

        assertThat(defaults.getConfiguredPathParamsMap()).hasSize(2)
                .containsEntry("p", 1)
                .containsEntry("p2", 2);

        assertThat(defaults.getConfiguredPropertiesMap()).hasSize(2)
                .containsEntry("PR", 1)
                .containsEntry("PR2", 2);

        assertThat(defaults.getConfiguredExtensionsMap()).hasSize(3)
                .containsEntry(Integer.class, Integer.class)
                .containsEntry(Object.class, extension)
                .containsEntry(String.class, "12");

        assertThat(defaults.getConfiguredFormDateFormatter()).isEqualTo(formatter);
        assertThat(defaults.getConfiguredFormDateTimeFormatter()).isEqualTo(DateTimeFormatter.ISO_DATE);

        assertThat(defaults.getConfiguredCookiesMap()).hasSize(3)
                .containsEntry("c1", new NewCookie.Builder("c1").value("1").build())
                .containsEntry("c2", new NewCookie.Builder("c2").value("11").build())
                .containsEntry("c3", new NewCookie.Builder("c3").value("12").build());

        assertThat(defaults.getConfiguredAccepts()).hasSize(1).containsOnly("application/json");
        assertThat(defaults.getConfiguredLanguages()).hasSize(1).containsOnly("en");
        assertThat(defaults.getConfiguredEncodings()).hasSize(1).containsOnly("gzip");
        assertThat(defaults.getConfiguredCacheControl()).isEqualTo(cacheControl);
        assertThat(defaults.getConfiguredPathModifiers()).hasSize(1).element(0).isEqualTo(pathFunc);
        assertThat(defaults.getConfiguredRequestModifiers()).hasSize(1).element(0).isEqualTo(reqFunc);
        assertThat(defaults.isDebugEnabled()).isTrue();
    }

    @Test
    void testCustomDefaults(ClientSupport support) {
        support.defaultAccept(MediaType.TEXT_HTML_TYPE)
                .defaultLanguage(Locale.CANADA)
                .defaultCacheControl(cacheControl);

        final TestRequestConfig defaults = support.getDefaults();
        assertThat(defaults.getConfiguredAccepts()).hasSize(1).containsOnly(MediaType.TEXT_HTML);
        assertThat(defaults.getConfiguredLanguages()).hasSize(1).containsOnly(Locale.CANADA.toString());
        assertThat(defaults.getConfiguredCacheControl()).isEqualTo(cacheControl);
    }

    @Test
    void testDefaultFlags(ClientSupport support) {
        assertThat(support.hasDefaultHeaders()).isFalse();
        support.defaultHeader("A", "a");
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultHeaders()).isTrue();

        assertThat(support.reset().hasDefaultQueryParams()).isFalse();
        support.defaultQueryParam("q", 1);
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultQueryParams()).isTrue();

        assertThat(support.reset().hasDefaultMatrixParams()).isFalse();
        support.defaultMatrixParam("m", 1);
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultMatrixParams()).isTrue();

        assertThat(support.reset().hasDefaultPathParams()).isFalse();
        support.defaultPathParam("p", 1);
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultPathParams()).isTrue();

        assertThat(support.reset().hasDefaultProperties()).isFalse();
        support.reset().defaultProperty("PR", 1);
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultProperties()).isTrue();

        assertThat(support.reset().hasDefaultExtensions()).isFalse();
        support.defaultRegister(Integer.class);
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultExtensions()).isTrue();

        assertThat(support.reset().hasDefaultFormDateFormatter()).isFalse();
        support.reset().defaultFormDateFormatter(formatter);
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultFormDateFormatter()).isTrue();

        assertThat(support.reset().hasDefaultFormDateFormatter()).isFalse();
        support.reset().defaultFormDateTimeFormatter(DateTimeFormatter.ISO_DATE);
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultFormDateFormatter()).isTrue();

        assertThat(support.reset().hasDefaultCookies()).isFalse();
        support.defaultCookie("c1", "1");
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultCookies()).isTrue();

        assertThat(support.reset().hasDefaultAccepts()).isFalse();
        support.defaultAccept("application/json");
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultAccepts()).isTrue();

        assertThat(support.reset().hasDefaultLanguages()).isFalse();
        support.defaultLanguage("en");
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultLanguages()).isTrue();

        assertThat(support.reset().hasDefaultEncodings()).isFalse();
        support.defaultEncoding("gzip");
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultEncodings()).isTrue();

        assertThat(support.reset().hasDefaultCacheControl()).isFalse();
        support.defaultCacheControl("max-age=604800, must-revalidate");
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultCacheControl()).isTrue();

        assertThat(support.reset().hasDefaultCustomConfigurators()).isFalse();
        support.defaultPathConfiguration(pathFunc);
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultCustomConfigurators()).isTrue();

        assertThat(support.reset().hasDefaultCustomConfigurators()).isFalse();
        support.defaultRequestConfiguration(reqFunc);
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultCustomConfigurators()).isTrue();

        assertThat(support.reset().isDebugEnabled()).isFalse();
        support.defaultDebug(true);
        assertThat(support.hasDefaults()).isFalse();
        assertThat(support.isDebugEnabled()).isTrue();

        assertThat(support.reset().hasDefaultFormDateFormatter()).isFalse();
        support.defaultFormDateFormat("yyyy");
        assertThat(support.hasDefaults()).isTrue();
        assertThat(support.hasDefaultFormDateFormatter()).isTrue();
    }
}
