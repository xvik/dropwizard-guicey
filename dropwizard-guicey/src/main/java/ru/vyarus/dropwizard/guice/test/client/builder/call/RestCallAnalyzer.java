package ru.vyarus.dropwizard.guice.test.client.builder.call;

import com.google.common.base.Preconditions;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.client.builder.FormBuilder;
import ru.vyarus.dropwizard.guice.test.client.builder.TestClientRequestBuilder;
import ru.vyarus.dropwizard.guice.url.model.ResourceMethodInfo;
import ru.vyarus.dropwizard.guice.url.resource.ResourceAnalyzer;
import ru.vyarus.dropwizard.guice.url.util.Caller;

/**
 * Allows using direct call for a resource method to configure rest client endpoint: target url is configured from
 * method {@link jakarta.ws.rs.Path} annotation. Optionally, {@link PathParam} and {@link QueryParam} parameters could
 * be configured by providing non-null values.
 * <p>
 * Limitation: this will work only with methods with DIRECTLY applied annotations: neither implemented interface method
 * (when annotations only on interface) nor overridden method would work.
 *
 * @author Vyacheslav Rusakov
 * @since 21.09.2025
 */
public final class RestCallAnalyzer {

    private RestCallAnalyzer() {
    }

    /**
     * Configure rest client by direct resource method call.
     * <p>
     * Internally, javassist proxy is used to intercept method call.
     * <p>
     * Limitation: this will work only with methods with DIRECTLY applied annotations: neither implemented interface
     * method (when annotations only on interface) nor overridden method would work.
     *
     * @param client rest client instance
     * @param method consumer with a required method call inside
     * @param body   body object (could be null)
     * @param <T>    resource type
     * @return pre-configured builder
     */
    public static <T> TestClientRequestBuilder configure(final ResourceClient<T> client, final Caller<T> method,
                                                         final @Nullable Object body) {
        final ResourceMethodInfo info = ResourceAnalyzer.analyzeMethodCall(client.getResourceType(), method);
        Object actualBody = body;
        if (actualBody == null && info.getEntity() != null) {
            // use provided argument without annotations as body
            // NOTE: both entity and form params are impossible
            actualBody = Entity.json(info.getEntity());
        }
        if (actualBody == null && !info.getFormParams().isEmpty()) {
            // build form entity
            final FormBuilder params = client.buildForm(info.getPath())
                    .params(info.getFormParams());
            // NOTE: GET method can't declare @FormParam so all form data would be declared as @QueryParam
            // force multipart form creation, even if there are no multipart values
            if (!info.getConsumes().isEmpty() && info.getConsumes().contains(MediaType.MULTIPART_FORM_DATA)) {
                params.forceMultipart();
            }
            // assume POST
            actualBody = params.buildEntity();
        }

        Preconditions.checkState(info.getHttpMethod() != null, "Called method lacks http method annotation");
        return client.build(info.getHttpMethod(), info.getPath(), actualBody)
                .pathParams(info.getPathParams())
                .queryParams(info.getQueryParams())
                .matrixParams(info.getMatrixParams())
                .headers(info.getHeaderParams())
                .cookies(info.getCookieParams());
    }

    /**
     * Resolve sub resource mapping path from lookup method.
     *
     * @param type   resource type
     * @param method lookup method caller
     * @param <T>    resource type
     * @return lookup method path
     */
    public static <T> String getSubResourcePath(final Class<T> type, final Caller<T> method) {
        final ResourceMethodInfo info = ResourceAnalyzer.analyzeMethodCall(type, method);
        return info.getPath();
    }
}
