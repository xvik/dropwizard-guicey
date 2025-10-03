package ru.vyarus.dropwizard.guice.url.model;

import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The result of {@link MethodCall} analysis. Used to pre-fill request
 * parameters by method annotations and provided arguments.
 * <p>
 * For sub resource call, {@link #subResources} would not be empty, {@link #resource} would point to the ROOT
 * resource and {@link #path} will be a method path, relative to resource root (include sub resource locator paths).
 *
 * @author Vyacheslav Rusakov
 * @since 25.09.2025
 */
public class ResourceMethodInfo {

    // resource class with annotations (but could be not for sub-resources)
    private final Class<?> resource;
    private final String resourcePath;

    private final List<Class<?>> subResources;
    private final List<Method> subResourceLocators;

    // method with annotations!
    private final Method method;
    private final String path;
    private final String httpMethod;

    private final List<String> consumes = new ArrayList<>();
    private final List<String> produces = new ArrayList<>();

    private final Map<String, Object> pathParams = new HashMap<>();
    private final Map<String, Object> queryParams = new HashMap<>();
    private final Map<String, Object> headerParams = new HashMap<>();
    private final Map<String, String> cookieParams = new HashMap<>();
    private final Map<String, Object> formParams = new HashMap<>();

    public ResourceMethodInfo(final Class<?> resource,
                              final String resourcePath,
                              final Method method,
                              final String path,
                              final @Nullable String httpMethod) {
        this(resource, resourcePath, method, path, httpMethod, null, null);
    }

    public ResourceMethodInfo(final Class<?> resource,
                              final String resourcePath,
                              final Method method,
                              final String path,
                              final @Nullable String httpMethod,
                              final @Nullable List<Class<?>> subResources,
                              final @Nullable List<Method> subResourceLocators) {
        this.resource = resource;
        this.resourcePath = resourcePath;
        this.method = method;
        this.path = path;
        this.httpMethod = httpMethod;
        this.subResources = subResources == null ? Collections.emptyList() : subResources;
        this.subResourceLocators = subResourceLocators == null ? Collections.emptyList() : subResourceLocators;
    }

    /**
     * Note: for sub-resources it is not required to have root {@link jakarta.ws.rs.Path} annotation, for such
     * classes here would be the root resource class.
     *
     * @return annotated resource class (where annotations declared)
     */
    public Class<?> getResource() {
        return resource;
    }

    /**
     * @return sub resources or empty if direct resource method
     */
    public List<Class<?>> getSubResources() {
        return subResources;
    }

    /**
     * @return sub resource locator methods, appeared in method call
     */
    public List<Method> getSubResourceLocators() {
        return subResourceLocators;
    }

    /**
     * @return resource path (without method), resolved from {@link jakarta.ws.rs.Path} annotation
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * This might not be directly called method, but superclass or interface method, where jersey annotations
     * were found (note that jersey does not collect annotations from different places - it expects everything to be
     * configured in one place).
     *
     * @return method with jersey annotations
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return {@link jakarta.ws.rs.Consumes} annotation value from method or class
     */
    public List<String> getConsumes() {
        return consumes;
    }

    /**
     * @return {@link jakarta.ws.rs.Produces} annotation value from method or clas
     */
    public List<String> getProduces() {
        return produces;
    }

    /**
     * @return map of not null arguments, passed for {@link jakarta.ws.rs.PathParam}
     */
    public Map<String, Object> getPathParams() {
        return pathParams;
    }

    /**
     * @return map of not null arguments, passed for {@link jakarta.ws.rs.QueryParam}
     */
    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    /**
     * @return map of not null arguments, passed for {@link jakarta.ws.rs.HeaderParam}
     */
    public Map<String, Object> getHeaderParams() {
        return headerParams;
    }

    /**
     * @return map of not null arguments, passed for {@link jakarta.ws.rs.CookieParam}
     */
    public Map<String, String> getCookieParams() {
        return cookieParams;
    }

    /**
     * @return map of not null arguments, passed for {@link jakarta.ws.rs.FormParam} or
     * {@link org.glassfish.jersey.media.multipart.FormDataParam}
     */
    public Map<String, Object> getFormParams() {
        return formParams;
    }

    /**
     * @return method path, resolved from {@link jakarta.ws.rs.Path} annotation
     */
    public String getPath() {
        return path;
    }

    /**
     * @return http method, resolved from {@link jakarta.ws.rs.HttpMethod} annotation inside
     * {@link jakarta.ws.rs.GET}, {@link jakarta.ws.rs.POST}, etc.
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * @return full method path ({@link #getResourcePath()} with {@link #getPath()})
     */
    public String getFullPath() {
        return PathUtils.path(resourcePath, path);
    }

    public void apply(final ResourceMethodInfo other) {
        // assuming processing method chain from left to right and so more specific consume or produce annotations
        // must override
        if (!other.consumes.isEmpty()) {
            this.consumes.clear();
            this.consumes.addAll(other.consumes);
        }
        if (!other.produces.isEmpty()) {
            this.produces.clear();
            this.produces.addAll(other.produces);
        }

        this.pathParams.putAll(other.pathParams);
        this.queryParams.putAll(other.queryParams);
        this.headerParams.putAll(other.headerParams);
        this.cookieParams.putAll(other.cookieParams);
        this.formParams.putAll(other.formParams);
    }

    @Override
    public String toString() {
        return resource.getSimpleName() + "." + TypeToStringUtils.toStringMethod(method, null)
                + " (" + getFullPath() + ")";
    }
}
