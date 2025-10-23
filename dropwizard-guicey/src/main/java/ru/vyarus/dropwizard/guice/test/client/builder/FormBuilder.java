package ru.vyarus.dropwizard.guice.test.client.builder;

import com.google.common.base.Preconditions;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import ru.vyarus.dropwizard.guice.test.client.builder.util.conf.FormParamsSupport;
import ru.vyarus.dropwizard.guice.test.client.util.MultipartCheck;
import ru.vyarus.dropwizard.guice.test.client.builder.util.conf.MultipartSupport;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Specialized builder to simplify the creation of urlencoded and multipart forms. This builder is just creating an
 * entity passed into usual GET or POST builders (these are the only possible methods for http forms).
 * <p>
 * Important: multipart support requires an additional dependency: 'org.glassfish.jersey.media:jersey-media-multipart'.
 * <p>
 * The type of the form is detected by provided values: if at least one value requires multipart, then multipart
 * entity is created, otherwise urlencoded entity created. Only urlencoded entity could be used with GET.
 * Multipart form type could be explicitly forced with {@link #forceMultipart()}.
 * <p>
 * Multipart values are: {@link java.io.File}, {@link java.io.InputStream}, or any
 * {@link org.glassfish.jersey.media.multipart.BodyPart} (including prepared fild objects like
 * {@link org.glassfish.jersey.media.multipart.FormDataBodyPart}.
 * <p>
 * Example urlencoded form usage:
 * <pre><code>
 *     buildForm(...)
 *     .param("name1", value1)
 *     .param("name2", value2)
 *     .buildPost()
 *     .invoke()
 * </code></pre>
 * <p>
 * Example multipart form usage:
 * <pre><code>
 *     buildForm(...)
 *     .param("name1", value1)
 *     .param("file", file)
 *     .buildPost()
 *     .invoke()
 * </code></pre>
 * <p>
 * Also, builder could be used just for entity building: {@link #buildEntity()} (for manual entity usage).
 * <p>
 * HTTP standard supports only GET and POST methods for forms, so there are no other shortcuts.
 *
 * @author Vyacheslav Rusakov
 * @since 15.09.2025
 */
public class FormBuilder {

    private final WebTarget target;
    private final TestRequestConfig config;
    private final Map<String, Object> formParams = new HashMap<>();

    private boolean multipart;

    /**
     * Create form builder.
     *
     * @param target form target
     * @param config inherited defaults
     */
    public FormBuilder(final WebTarget target, final TestRequestConfig config) {
        this.target = target;
        this.config = new TestRequestConfig(config);
    }

    /**
     * Normally, a form type is detected from the provided values: if at least one value requires multipart, then
     * multipart type selected, otherwise urlencoded used.
     * <p>
     * This option forces multipart type usage, even if it is not required according to provided values.
     *
     * @return builder instance for chained calls
     */
    public FormBuilder forceMultipart() {
        this.multipart = true;
        return this;
    }

    /**
     * Java.util date formatter for formatting date parameters.
     *
     * @param formatter date formatter
     * @return builder instance for chained calls
     * @see #dateFormat(String) for short declaration
     */
    public FormBuilder dateFormatter(final DateFormat formatter) {
        this.config.formDateFormatter(formatter);
        return this;
    }

    /**
     * Java.time formatter for formatting date parameters.
     *
     * @param formatter date formatter
     * @return builder instance for chained calls
     * @see #dateFormat(String) for short declaration
     */
    public FormBuilder dateTimeFormatter(final DateTimeFormatter formatter) {
        this.config.formDateTimeFormatter(formatter);
        return this;
    }

    /**
     * Shortcut to configure both date formatters with the same pattern.
     *
     * @param format format
     * @return builder instance for chained calls
     * @see #dateFormatter(java.text.DateFormat)
     * @see #dateTimeFormatter(java.time.format.DateTimeFormatter)
     */
    @SuppressWarnings("PMD.SimpleDateFormatNeedsLocale")
    public FormBuilder dateFormat(final String format) {
        this.config.formDateFormatter(new SimpleDateFormat(format));
        this.config.formDateTimeFormatter(DateTimeFormatter.ofPattern(format));
        return this;
    }

    /**
     * Add form parameter. Multiple values could be provided with collection or array.
     * <p>
     * {@link File} or {@link java.io.InputStream} or anything derived from
     * {@link org.glassfish.jersey.media.multipart.BodyPart} would lead to multipart form creation. For multipart
     * requests, value could be provided in form of manually constructed fiels, like
     * {@link org.glassfish.jersey.media.multipart.FormDataMultiPart} (any body part implementation).
     * <p>
     * For other value types, value is converted to string:
     * <ul>
     *     <li>Date fields string conversion could be customized with date formatters (one for java.util and other for
     *        java.time api).</li>
     *     <li>Null values converted to ""</li>
     *     <li>Collections converted to a comma-separated string of values (converted to string)</li>
     *     <li>By default, call toString on provided object</li>
     * </ul>
     * <p>
     * Custom date formatters could be set as a default for all requests with:
     * {@link ru.vyarus.dropwizard.guice.test.client.TestClient#defaultFormDateFormatter(java.text.DateFormat)} and
     * {@link ru.vyarus.dropwizard.guice.test.client.TestClient#defaultFormDateTimeFormatter(
     * java.time.format.DateTimeFormatter)}.
     * Or for request with: {@link #dateFormatter(java.text.DateFormat)} and
     * {@link #dateTimeFormatter(java.time.format.DateTimeFormatter)}.
     * <p>
     * Multiple calls override previously declared parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @return builder instance for chained calls
     */
    public FormBuilder param(final String name, final Object value) {
        formParams.put(name, value);
        return this;
    }

    /**
     * Same as {@link #param(String, Object)} but provides multiple parameters for the same name.
     *
     * @param name   parameter name
     * @param values parameter values
     * @return builder instance for chained calls
     */
    public FormBuilder param(final String name, final Object... values) {
        return param(name, Arrays.asList(values));
    }

    /**
     * Adds multiple form params. Multiple values could be provided with collection or array.
     *
     * @param params form params map.
     * @return builder instance for chained calls
     */
    public FormBuilder params(final Map<String, Object> params) {
        formParams.putAll(params);
        return this;
    }

    /**
     * Build POST request builder for a form. This is the same as calling
     * {@link ru.vyarus.dropwizard.guice.test.client.TestClient#buildPost(String, Object, Object...)}, but with
     * pre-build entity.
     *
     * @return POST request builder
     */
    public TestClientRequestBuilder buildPost() {
        return new TestClientRequestBuilder(target, HttpMethod.POST, buildEntity(), config);
    }

    /**
     * Build GET request builder for a form. This is the same as calling
     * {@link ru.vyarus.dropwizard.guice.test.client.TestClient#buildGet(String, Object...)}, but with pre-build
     * query params.
     * <p>
     * Call will be failed if multipart form creation is required (which can't be handled with GET).
     *
     * @return GET request builder
     */
    public TestClientRequestBuilder buildGet() {
        Preconditions.checkState(!isMultipart(), "Multipart form can't be sent with GET");
        return new TestClientRequestBuilder(target, HttpMethod.GET, null, config)
                .queryParams(buildQueryParams());
    }

    /**
     * Build entity for manual usage in calls.
     * <p>
     * For example, could be used in manual post call (as body):
     * {@code post("/somePath/", form).invoke()}.
     *
     * @return entity (form body)
     */
    public Entity<?> buildEntity() {
        Preconditions.checkState(!formParams.isEmpty(), "At least one form param is required");
        final boolean multipart = this.multipart || isMultipart();

        if (!multipart) {
            final Form form = new Form();
            formParams.forEach((s, o) -> {
                if (o instanceof Collection) {
                    for (Object v : ((Collection) o)) {
                        form.param(s, FormParamsSupport.parameterToString(v,
                                config.getConfiguredFormDateFormatter(),
                                config.getConfiguredFormDateTimeFormatter()));
                    }
                } else if (o.getClass().isArray()) {
                    for (Object v : ((Object[]) o)) {
                        form.param(s, FormParamsSupport.parameterToString(v,
                                config.getConfiguredFormDateFormatter(),
                                config.getConfiguredFormDateTimeFormatter()));
                    }
                } else {
                    form.param(s, FormParamsSupport.parameterToString(o,
                            config.getConfiguredFormDateFormatter(),
                            config.getConfiguredFormDateTimeFormatter()));
                }
            });
            return Entity.form(form);
        }

        MultipartCheck.requireEnabled();
        return MultipartSupport.buildMultipart(formParams,
                config.getConfiguredFormDateFormatter(),
                config.getConfiguredFormDateTimeFormatter());
    }

    /**
     * Build query parameters for manual usage in GET calls.
     * <p>
     * For example:
     * {@code buildGet("/somePath/").queryParams(params).invoke())}
     * <p>
     * Parameters with multiple values would contain lists of values.
     *
     * @return parameters map to be used in GET request
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> buildQueryParams() {
        Preconditions.checkState(!formParams.isEmpty(), "At least one form param is required");
        final Map<String, Object> result = new HashMap<>();
        formParams.forEach((s, o) -> {
            if (o instanceof Collection) {
                result.put(s, handleMultivalue((Collection) o));
            } else if (o.getClass().isArray()) {
                result.put(s, handleMultivalue((Object[]) o));
            } else {
                result.put(s, FormParamsSupport.parameterToString(o,
                        config.getConfiguredFormDateFormatter(),
                        config.getConfiguredFormDateTimeFormatter()));
            }
        });
        return result;
    }

    @Override
    public String toString() {
        return "Form builder for: " + target.getUri().toString();
    }

    private List<String> handleMultivalue(final Collection<Object> values) {
        final List<String> res = new ArrayList<>();
        for (Object v : values) {
            res.add(FormParamsSupport.parameterToString(v,
                    config.getConfiguredFormDateFormatter(),
                    config.getConfiguredFormDateTimeFormatter()));
        }
        return res;
    }

    private List<String> handleMultivalue(final Object... values) {
        final List<String> res = new ArrayList<>();
        for (Object v : values) {
            res.add(FormParamsSupport.parameterToString(v,
                    config.getConfiguredFormDateFormatter(),
                    config.getConfiguredFormDateTimeFormatter()));
        }
        return res;
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    private boolean isMultipart() {
        for (Object value : formParams.values()) {
            if (isMultipart(value)) {
                return true;
            }
            // case: multiple values with the same name
            if (value instanceof Collection) {
                for (Object v : ((Collection<?>) value)) {
                    if (isMultipart(v)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isMultipart(final Object value) {
        return value instanceof File
                || value instanceof InputStream
                // includes BodyPart and other file mapping fields (not directly BodyPart to support case
                // when multipart jar not declared)
                || (MultipartCheck.isEnabled() && MultipartSupport.isMultipartValue(value));
    }
}
