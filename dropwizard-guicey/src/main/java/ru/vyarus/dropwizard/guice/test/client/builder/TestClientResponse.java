package ru.vyarus.dropwizard.guice.test.client.builder;

import com.google.common.base.Preconditions;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.eclipse.jetty.http.HttpHeader;
import org.glassfish.jersey.message.internal.CacheControlProvider;
import org.junit.jupiter.api.Assertions;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.builder.TestSupportHolder;
import ru.vyarus.dropwizard.guice.test.client.util.FileDownloadUtil;
import ru.vyarus.dropwizard.guice.test.client.builder.util.TestClientResponseCleanup;
import ru.vyarus.java.generics.resolver.context.container.ParameterizedTypeImpl;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Wrapper for jersey {@link jakarta.ws.rs.core.Response} with extra assertion shortcuts (jersey response object
 * is general-purpose and this api assumed to be used for tests).
 * <p>
 * Provides the following method groups:
 * <ul>
 *     <li>"as*" methods convert the response body (shortcuts for
 *     {@link jakarta.ws.rs.core.Response#readEntity(Class)})</li>
 *     <li>"assert*" methods to simplify response assertions. All these methods use junit 5 assertions (methods with
 *     {@link java.util.function.Predicate} also use assertions internally).</li>
 *     <li>"with*" methods for manual operations with various objects (to not create additional variable in test)</li>
 * </ul>
 * <p>
 * {@link jakarta.ws.rs.core.Response} would close only if response body was read. To indicate the importance of
 * manual close, {@link java.lang.AutoCloseable} is implemented (same as in response) and so IDEA would
 * highlight usage without "try-with-resources". You could ignore it when the client is used with guicey test
 * extensions, as all responses would be closed just after the test application shutdown.
 * <p>
 * Original response object is accessible with {@link #asResponse()}.
 *
 * @author Vyacheslav Rusakov
 * @since 12.09.2025
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.TooManyMethods", "checkstyle:MultipleStringLiterals"})
public class TestClientResponse implements AutoCloseable {

    private static final String RESPONSE_DOES_NOT_MATCH = "Response does not match condition";
    private final Response response;

    /**
     * Create a client response wrapper.
     *
     * @param response response object to wrap
     */
    public TestClientResponse(final Response response) {
        this.response = response;
        // response might be not closed (if the body is not read) keep tracking response until the application shutdown
        registerResponse(response);
    }

    /**
     * Warning: there is no explicit check for the request success state because this method might be used to read
     * the error body. Use {@link #assertSuccess()} explicitly to make sure request was successful.
     *
     * @return response body as string
     */
    public String asString() {
        return response.readEntity(String.class);
    }

    /**
     * Shortcut method to avoid {@link jakarta.ws.rs.core.GenericType} usage for simple lists (same as
     * {@code response.readEntity(new GenericType&lt;List&lt;EntityType&gt;&gt;(){})}).
     * <p>
     * Warning: there is no explicit check for the request success state because this method might be used to read
     * the error body. Use {@link #assertSuccess()} explicitly to make sure request was successful.
     *
     * @param entityType list entities type
     * @param <T>        entity type
     * @return response body, read as list
     * @throws java.lang.IllegalStateException if entity was already read (e.g. by {@link #as(Class)})
     */
    public <T> List<T> asList(final Class<T> entityType) {
        return response.readEntity(new GenericType<>(new ParameterizedTypeImpl(List.class, entityType)));
    }

    /**
     * Read response body as declared type (same as {@link jakarta.ws.rs.core.Response#readEntity(Class)}).
     * For complex types use {@link #as(GenericType)}.
     * <p>
     * Warning: there is no explicit check for the request success state because this method might be used to read
     * the error body. Use {@link #assertSuccess()} explicitly to make sure request was successful.
     *
     * @param entityType entity type
     * @param <T>        entity type
     * @return result mapped into entity
     * @throws java.lang.IllegalStateException if entity was already read
     */
    public <T> T as(final Class<T> entityType) {
        return response.readEntity(entityType);
    }

    /**
     * Required for types with generics.For example: {@code .as(new GenericType<Something<Else>>(){})}.
     * <p>
     * Note: when the result is assigned to variable, there is no need to specify type - use diamond operator
     * ({@code new GenericType<>(){}}) (but there is no way to avoid specifying generic type at all).
     * <p>
     * For simple list cases use {@link #asList(Class)}
     * <p>
     * Warning: there is no explicit check for the request success state because this method might be used to read
     * the error body. Use {@link #assertSuccess()} explicitly to make sure request was successful.
     *
     * @param entityType entity type
     * @param <T>        entity type
     * @return result mapped into entity
     * @throws java.lang.IllegalStateException if entity was already read
     */
    public <T> T as(final GenericType<T> entityType) {
        return response.readEntity(entityType);
    }

    /**
     * General response conversion logic. Exists for chained calls - to avoid redundant variable usage in the test.
     * <p>
     * Warning: there is no explicit check for the request success state because this method might be used to read
     * the error body. Use {@link #assertSuccess()} explicitly to make sure request was successful.
     *
     * @param converter response converter.
     * @param <T>       entity type
     * @return conversion result
     */
    public <T> T as(final Function<Response, T> converter) {
        return converter.apply(response);
    }

    /**
     * @return raw response object
     */
    public Response asResponse() {
        return response;
    }

    /**
     * Download the response file into a directory. In contrast to {@code response.readEntity(File.class)}, this
     * method preserves file name (if provided) and creates not temporary file. If file name collide with already
     * existing file, created file name would be modified with "(index)",
     * <p>
     * Usually it's easier to manage local temp directory in test rather than rely on global temp.
     * <p>
     * Warning: there is no explicit check for the request success state because this method might be used to read
     * the error body. Use {@link #assertSuccess()} explicitly to make sure request was successful.
     *
     * @param tmpDir temporary files directory
     * @return downloaded file path
     */
    public Path asFile(final Path tmpDir) {
        return FileDownloadUtil.download(asResponse(), tmpDir);
    }

    /**
     * Close the underlying response object. This is required ONLY when the response body was not acquired by any of
     * as* methods (excluding asResponse).
     * <p>
     * It is not required to call it explicitly when client is used with guicey test extensions because guicey would
     * close all used responses after test application shutdown.
     */
    @Override
    public void close() {
        response.close();
    }


    // ------------------------------------------------------------ ASSERTIONS

    /**
     * Assert a mapped response with custom condition.
     * <p>
     * For custom assertions use {@link #withResponse(Class, java.util.function.Consumer)}.
     *
     * @param entityType entity type
     * @param predicate  assertion condition
     * @param <T>        entity type
     * @return response instance for chained calls
     * @throws java.lang.IllegalStateException if entity was already read (e.g. by {@link #as(Class)})
     */
    public <T> TestClientResponse assertResponse(final Class<T> entityType, final Predicate<T> predicate) {
        Assertions.assertTrue(predicate.test(as(entityType)), RESPONSE_DOES_NOT_MATCH);
        return this;
    }

    /**
     * Assert a mapped response with custom condition.
     * <p>
     * For custom assertions use {@link #withResponse(jakarta.ws.rs.core.GenericType, java.util.function.Consumer)}.
     *
     * @param entityType entity type
     * @param predicate  assertion condition
     * @param <T>        entity type
     * @return response instance for chained calls
     * @throws java.lang.IllegalStateException if entity was already read (e.g. by {@link #as(Class)})
     */
    public <T> TestClientResponse assertResponse(final GenericType<T> entityType, final Predicate<T> predicate) {
        Assertions.assertTrue(predicate.test(as(entityType)), RESPONSE_DOES_NOT_MATCH);
        return this;
    }

    /**
     * Assert a response object (general assertion logic).
     * <p>
     * For custom assertions use {@link #withResponse(java.util.function.Consumer)}.
     *
     * @param consumer response assertion
     * @return response instance for chained calls
     */
    public TestClientResponse assertResponse(final Predicate<Response> consumer) {
        return withResponse(response -> Assertions
                .assertTrue(consumer.test(response), RESPONSE_DOES_NOT_MATCH));
    }

    /**
     * Assert response status to be one of the provided statuses. Note that often it's simpler to check for status
     * family using {@link #assertStatus(jakarta.ws.rs.core.Response.Status.Family)}.
     * <p>
     * For custom assertions use {@link #withStatus(java.util.function.Consumer)}.
     *
     * @param expectedStatuses expected statuses
     * @return response instance for chained calls
     * @see org.apache.hc.core5.http.HttpStatus
     */
    public TestClientResponse assertStatus(final Integer... expectedStatuses) {
        Preconditions.checkArgument(expectedStatuses.length > 0, "At least one status is required");
        Assertions.assertTrue(Arrays.asList(expectedStatuses).contains(response.getStatus()),
                () -> "Unexpected response status " + response.getStatus()
                        + " when expected " + Arrays.stream(expectedStatuses)
                        .map(Object::toString)
                        .collect(Collectors.joining(" or ")));
        return this;
    }

    /**
     * Assert status family (family is a first digit in status code so SUCCESS family would mean any of 200, 201,
     * 204, etc.).
     * <p>
     * For custom assertions use {@link #withStatus(java.util.function.Consumer)}.
     *
     * @param family expected family
     * @return response instance for chained calls
     */
    public TestClientResponse assertStatus(final Response.Status.Family family) {
        return withStatus(statusType -> Assertions
                .assertEquals(family, statusType.getFamily(), "Expected '" + family
                        + "' response status, but found '" + statusType.getFamily() + "'"));
    }

    /**
     * Assert status object.
     * <p>
     * For custom assertions use {@link #withStatus(java.util.function.Consumer)}.
     *
     * @param predicate predicate to check status object
     * @return response instance for chained calls
     */
    public TestClientResponse assertStatus(final Predicate<Response.StatusType> predicate) {
        return withStatus(statusType -> Assertions
                .assertTrue(predicate.test(statusType), "Response status '" + statusType.getStatusCode() + " "
                        + statusType.getReasonPhrase() + "' does not match condition"));
    }

    /**
     * Assert response status to be 2xx.
     * <p>
     * For custom assertions use {@link #withStatus(java.util.function.Consumer)}.
     *
     * @return response instance for chained calls
     */
    public TestClientResponse assertSuccess() {
        // all 2xx
        return assertStatus(Response.Status.Family.SUCCESSFUL);
    }

    /**
     * Assert response status is not 2xx (not success). Note that redirection also falls into this category,
     * but, as normally, the client follows redirects, then it should not be a problem.
     * <p>
     * For custom assertions use {@link #withStatus(java.util.function.Consumer)}.
     *
     * @return response instance for chained calls
     */
    public TestClientResponse assertFail() {
        return withStatus(statusType ->
                Assertions.assertNotEquals(Response.Status.Family.SUCCESSFUL, statusType.getFamily(),
                        "Failed response expected, but found '" + statusType.getFamily() + "'"));
    }

    /**
     * Assert response status is 3xx. Note that by default the client follows redirects, so it's important to disable
     * it to assert redirection correctness.
     * <p>
     * For custom assertions use {@link #withStatus(java.util.function.Consumer)}.
     *
     * @return response instance for chained calls
     */
    public TestClientResponse assertRedirect() {
        return assertStatus(Response.Status.Family.REDIRECTION);
    }

    /**
     * Assert cache control header value.
     * <p>
     * For custom assertions use {@link #withCacheControl(java.util.function.Consumer)}.
     *
     * @param predicate assertion condition
     * @return response instance for chained calls
     */
    public TestClientResponse assertCacheControl(final Predicate<CacheControl> predicate) {
        return assertHeader(HttpHeader.CACHE_CONTROL, s -> predicate
                .test(new CacheControlProvider().fromString(s)));
    }

    /**
     * Assert the response have no body.
     * <p>
     * For custom assertions use {@link #withResponse(java.util.function.Consumer)}
     *
     * @return response instance for chained calls
     * @throws java.lang.IllegalStateException if entity was already read (e.g. by {@link #as(Class)})
     */
    public TestClientResponse assertVoidResponse() {
        Assertions.assertFalse(response.hasEntity(), "Void response expected, but found: \n" + asString());
        return this;
    }

    /**
     * Assert response media type.
     *
     * @param mediaType expected media type
     * @return response instance for chained calls
     */
    public TestClientResponse assertMedia(final MediaType mediaType) {
        Assertions.assertEquals(mediaType, response.getMediaType(), "Expected '" + mediaType
                + "' media type, but found '" + response.getMediaType() + "'");
        return this;
    }

    /**
     * Assert response locale.
     *
     * @param locale expected locale
     * @return response instance for chained calls
     */
    public TestClientResponse assertLocale(final Locale locale) {
        Assertions.assertEquals(locale, response.getLanguage(), "Expected '" + locale
                + "' response locale, but found '" + response.getLanguage() + "'");
        return this;
    }

    /**
     * Assert header value. In case of multiple headers present, its values would be concatenated with "," and the
     * resulting string would be compared with the expected value.
     * <p>
     * For custom assertions use {@link #withHeader(org.eclipse.jetty.http.HttpHeader, java.util.function.Consumer)}.
     *
     * @param name  header name
     * @param value expected value
     * @return response instance for chained calls
     */
    public TestClientResponse assertHeader(final String name, final String value) {
        return withHeader(name, s -> Assertions.assertEquals(value, s,
                "Expected header '" + name + "' value '" + value + "', but found '" + s + "'"));
    }

    /**
     * Assert header value with predicate. In case of multiple headers present, its values would be concatenated
     * with "," and the resulting string would be compared with the expected value.
     * <p>
     * For custom assertions use {@link #withHeader(org.eclipse.jetty.http.HttpHeader, java.util.function.Consumer)}.
     *
     * @param name      header name
     * @param predicate value predicate
     * @return response instance for chained calls
     */
    public TestClientResponse assertHeader(final String name, final Predicate<String> predicate) {
        return withHeader(name, s -> Assertions.assertTrue(predicate.test(s),
                "Header '" + name + ": " + s + "' does not match condition")
        );
    }

    /**
     * Assert header value. In case of multiple headers present, its values would be concatenated with "," and the
     * resulting string would be compared with the expected value.
     * <p>
     * For custom assertions use {@link #withHeader(org.eclipse.jetty.http.HttpHeader, java.util.function.Consumer)}.
     *
     * @param header header name
     * @param value  expected value
     * @return response instance for chained calls
     */
    public TestClientResponse assertHeader(final HttpHeader header, final String value) {
        return assertHeader(header.toString(), value);
    }

    /**
     * Assert header value with predicate. In case of multiple headers present, its values would be concatenated
     * with "," and the resulting string would be provided to the predicate for evaluation.
     * <p>
     * For custom assertions use {@link #withHeader(org.eclipse.jetty.http.HttpHeader, java.util.function.Consumer)}.
     *
     * @param predicate value predicate
     * @param header    header name
     * @return response instance for chained calls
     */
    public TestClientResponse assertHeader(final HttpHeader header, final Predicate<String> predicate) {
        return assertHeader(header.toString(), predicate);
    }

    /**
     * Assert cookie value.
     * <p>
     * For custom assertions use {@link #withCookie(String, java.util.function.Consumer)}.
     *
     * @param name  cookie name
     * @param value expected value
     * @return response instance for chained calls
     */
    public TestClientResponse assertCookie(final String name, final String value) {
        return withCookie(name, cookie -> Assertions.assertEquals(value, cookie.getValue(),
                "Expected cookie '" + name + ": " + value + "', but found '" + cookie.getValue() + "'"));
    }

    /**
     * Assert cookie value with a predicate.
     * <p>
     * For custom assertions use {@link #withCookie(String, java.util.function.Consumer)}.
     *
     * @param name      cookie name
     * @param predicate value predicate
     * @return response instance for chained calls
     */
    public TestClientResponse assertCookie(final String name, final Predicate<Cookie> predicate) {
        return withCookie(name, cookie -> Assertions
                .assertTrue(predicate.test(cookie), "Cookie '" + RuntimeDelegate.getInstance()
                        .createHeaderDelegate(Cookie.class).toString(cookie) + "' does not match condition"));
    }

    // ------------------------------------------------------------ MANUAL CHECKS

    /**
     * Assert response body mapping and the resulted object.
     *
     * @param entityType entity type
     * @param consumer   mapped result consumer
     * @return response instance for chained calls
     * @throws java.lang.IllegalStateException if entity was already read (e.g. by {@link #as(Class)})
     * @param <T>        entity type
     */
    public <T> TestClientResponse withResponse(final Class<T> entityType, final Consumer<T> consumer) {
        consumer.accept(as(entityType));
        return this;
    }

    /**
     * Assert response body mapping and the resulted object.
     *
     * @param entityType entity type
     * @param consumer   mapped result consumer
     * @return response instance for chained calls
     * @throws java.lang.IllegalStateException if entity was already read (e.g. by {@link #as(Class)})
     * @param <T>        entity type
     */
    public <T> TestClientResponse withResponse(final GenericType<T> entityType, final Consumer<T> consumer) {
        consumer.accept(as(entityType));
        return this;
    }

    /**
     * Manually examine a response object and perform manual assertions.
     *
     * @param consumer response object consumer
     * @return response instance for chained calls
     */
    public TestClientResponse withResponse(final Consumer<Response> consumer) {
        consumer.accept(response);
        return this;
    }

    /**
     * Manually examine a status object and perform assertions.
     *
     * @param consumer status object consumer
     * @return response instance for chained calls
     */
    public TestClientResponse withStatus(final Consumer<Response.StatusType> consumer) {
        consumer.accept(response.getStatusInfo());
        return this;
    }

    /**
     * Manually examine header and perform assertions. In case of multiple headers present, its values would be
     * concatenated with "," and the resulting string would be provided for manual assertions.
     *
     * @param header   header name
     * @param consumer value consumer
     * @return response instance for chained calls
     */
    public TestClientResponse withHeader(final HttpHeader header, final Consumer<String> consumer) {
        return withHeader(header.toString(), consumer);
    }

    /**
     * Manually examine header and perform assertions. In case of multiple headers present, its values would be
     * concatenated with "," and the resulting string would be provided for manual assertions.
     *
     * @param name     header name
     * @param consumer value consumer
     * @return response instance for chained calls
     */
    public TestClientResponse withHeader(final String name, final Consumer<String> consumer) {
        if (response.getHeaders().containsKey(name)) {
            consumer.accept(response.getHeaderString(name));
        } else {
            final String headers = response.getHeaders().keySet().stream()
                    .map(key -> key + "=" + response.getHeaderString(key))
                    .collect(Collectors.joining("\n"));
            Assertions.fail("Missing header '" + name + "' in response. Available headers: \n" + headers);
        }
        return this;
    }

    /**
     * Manually examine cookie and perform assertions.
     *
     * @param name     cookie name
     * @param consumer cookie object consumer
     * @return response instance for chained calls
     */
    public TestClientResponse withCookie(final String name, final Consumer<Cookie> consumer) {
        if (response.getCookies().containsKey(name)) {
            consumer.accept(response.getCookies().get(name));
        } else {
            final String cookies = response.getCookies().values().stream()
                    .map(key -> key.getName() + "=" + key.getValue())
                    .collect(Collectors.joining("\n"));
            Assertions.fail("Missing cookie '" + name + "' in response. Available cookies: \n"
                    + cookies);
        }
        return this;
    }

    /**
     * Manually examine the cache control header and perform assertions.
     *
     * @param consumer cache control consumer
     * @return response instance for chained calls
     */
    public TestClientResponse withCacheControl(final Consumer<CacheControl> consumer) {
        return withHeader(HttpHeader.CACHE_CONTROL, s -> consumer
                .accept(new CacheControlProvider().fromString(s)));
    }

    @Override
    public String toString() {
        return "Response: " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase();
    }

    private static void registerResponse(final Response response) {
        // Only when outer context is present (all junit extensions and most generic test cases)
        // Using shared state to automate cleanup just after application shutdown.
        // This isn't intended to be a 100% guarantee, just a help for some cases (that's why no errors)
        if (TestSupportHolder.isContextSet()) {
            SharedConfigurationState.lookupOrCreate(TestSupport.getContext().getEnvironment(),
                            TestClientResponseCleanup.class, TestClientResponseCleanup::new)
                    .add(response);
        }
    }
}
