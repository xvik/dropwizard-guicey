package ru.vyarus.guicey.gsp.views.test.ext;

import java.lang.reflect.Method;

/**
 * Intercepted view model class with context info (required to distinguish models when multiple view calls
 * is intercepted at once).
 * <p>
 * {@link #getPath()} would contain a real called path (before redirection to view resource),
 * {@link #getResourcePath()} would contain view resource path.
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2025
 */
public class ViewModel {
    private String path;
    private String httpMethod;
    private String resourcePath;
    private Class<?> resourceClass;
    private Method resourceMethod;
    private Object model;
    private int statusCode;

    /**
     * @return called path (before redirection to resource) relative to server root
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path original path
     */
    public void setPath(final String path) {
        this.path = path;
    }

    /**
     * @return view resource path relative to rest context
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * @param resourcePath view resource path
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
     * @return view resource class
     */
    public Class<?> getResourceClass() {
        return resourceClass;
    }

    /**
     * @param resourceClass view resource class
     */
    public void setResourceClass(final Class<?> resourceClass) {
        this.resourceClass = resourceClass;
    }

    /**
     * @return view resource method
     */
    public Method getResourceMethod() {
        return resourceMethod;
    }

    /**
     * @param resourceMethod view resource method
     */
    public void setResourceMethod(final Method resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    /**
     * @param <T> required type to simplify usage in tests
     * @return view model
     */
    @SuppressWarnings("unchecked")
    public <T> T getModel() {
        return (T) model;
    }

    /**
     * @param model view model
     */
    public void setModel(final Object model) {
        this.model = model;
    }

    /**
     * Note that code might be 200 here, but view rendering would fail later (model intercepted before view rendering).
     * This code is preserved just to differentiate error views from normal pages (when error models interception is
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
        return httpMethod + " " + path + " (" + resourceClass.getSimpleName() + "#" + resourceMethod.getName() + ")";
    }
}
