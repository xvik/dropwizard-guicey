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
    // in case of sub resources each method call analyzed separately
    private final List<ResourceMethodInfo> steps;

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
    private final Map<String, Object> matrixParams = new HashMap<>();

    private Object entity;

    /**
     * Create a resource method info object.
     *
     * @param resource     resource class
     * @param resourcePath resource path
     * @param method       analyzed method
     * @param path         method path
     * @param httpMethod   http method
     */
    public ResourceMethodInfo(final Class<?> resource,
                              final String resourcePath,
                              final Method method,
                              final String path,
                              final @Nullable String httpMethod) {
        this(resource, resourcePath, method, path, httpMethod, null, null);
    }

    /**
     * Create a resource method info object for the call chain (including sub resource locators).
     *
     * @param resource     resource class
     * @param resourcePath resource path
     * @param method       analyzed method
     * @param path         method path
     * @param httpMethod   http method
     * @param subResources sub resource classes
     * @param steps        separate analysis for each method call in the chain
     */
    public ResourceMethodInfo(final Class<?> resource,
                              final String resourcePath,
                              final Method method,
                              final String path,
                              final @Nullable String httpMethod,
                              final @Nullable List<Class<?>> subResources,
                              final @Nullable List<ResourceMethodInfo> steps) {
        this.resource = resource;
        this.resourcePath = resourcePath;
        this.method = method;
        this.path = path;
        this.httpMethod = httpMethod;
        this.subResources = subResources == null ? Collections.emptyList() : subResources;
        this.steps = steps == null ? Collections.emptyList() : steps;
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
     * When sub resources used there would be multiple method calls {@code resource.subResource().method()}.
     * In this case, each method call analyzed separately and, in some cases, non-aggregated data might be important
     * (e.g. the only way to properly handle matrix params).
     *
     * @return separate methods analysis results for sub resource lookups or empty for single method call
     */
    public List<ResourceMethodInfo> getSteps() {
        return steps;
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
     * @return map of not null arguments, passed for {@link jakarta.ws.rs.MatrixParam}.
     */
    public Map<String, Object> getMatrixParams() {
        return matrixParams;
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
     * @return entity object
     */
    public Object getEntity() {
        return entity;
    }

    /**
     * Provided argument without known jersey annotations (assumed to be request body).
     *
     * @param entity entity object
     */
    public void setEntity(final Object entity) {
        this.entity = entity;
    }

    /**
     * @return full method path ({@link #getResourcePath()} with {@link #getPath()})
     */
    public String getFullPath() {
        return PathUtils.path(resourcePath, path);
    }

    /**
     * Apply other method info to this one.
     *
     * @param other other data
     */
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
        this.matrixParams.putAll(other.matrixParams);
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
