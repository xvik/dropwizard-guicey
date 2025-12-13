package ru.vyarus.dropwizard.guice.test.responsemodel.model;

import java.lang.reflect.Method;

/**
 * Intercepted response model with context info (required to distinguish models when multiple calls intercepted at
 * once). The actual model would be either entity returned from response method or entity supplied manually into
 * {@link jakarta.ws.rs.core.Response} (responses without a model are not ignored).
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2025
 */
public class ResponseModel {
    private String httpMethod;
    private String resourcePath;
    private Class<?> resourceClass;
    private Method resourceMethod;
    private Object model;
    private int statusCode;

    /**
     * @return resource path relative to the rest context
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * @param resourcePath resource path
     */
    public void setResourcePath(final String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * @return HTTP method
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * @param httpMethod HTTP method
     */
    public void setHttpMethod(final String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * @return resource class
     */
    public Class<?> getResourceClass() {
        return resourceClass;
    }

    /**
     * @param resourceClass resource class
     */
    public void setResourceClass(final Class<?> resourceClass) {
        this.resourceClass = resourceClass;
    }

    /**
     * @return resource method
     */
    public Method getResourceMethod() {
        return resourceMethod;
    }

    /**
     * @param resourceMethod resource method
     */
    public void setResourceMethod(final Method resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    /**
     * @param <T> required type to simplify usage in tests
     * @return response model (object returned by resource method or directly specified as Entity in
     * {@link jakarta.ws.rs.core.Response})
     */
    @SuppressWarnings("unchecked")
    public <T> T getModel() {
        return (T) model;
    }

    /**
     * @param model entity returned from resource method
     */
    public void setModel(final Object model) {
        this.model = model;
    }

    /**
     * Note that code might be 200 here, but response processing will fail later (e.g. view rendering would fail later).
     * This code is preserved just to differentiate error models from normal (when error models interception is
     * enabled).
     *
     * @return response status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode response status code
     */
    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return httpMethod + " " + statusCode + " " + resourcePath + " (" + resourceClass.getSimpleName()
                + "#" + resourceMethod.getName() + ")";
    }
}
