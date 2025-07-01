package ru.vyarus.guicey.gsp.app.rest.log;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;

import java.util.Comparator;

/**
 * Represents template rest method.
 *
 * @author Vyacheslav Rusakov
 * @since 03.12.2018
 */
public class ViewPath implements Comparable<ViewPath> {

    private final ResourceMethod method;
    private final Resource resource;
    private final Class<?> resourceType;
    private final String url;

    /**
     * Create a view path.
     *
     * @param method       method
     * @param resource     resource
     * @param resourceType resource class
     * @param url          url
     */
    public ViewPath(final ResourceMethod method,
                    final Resource resource,
                    final Class<?> resourceType,
                    final String url) {
        this.method = method;
        this.resource = resource;
        this.resourceType = resourceType;
        this.url = url;
    }

    /**
     * @return resource method
     */
    public ResourceMethod getMethod() {
        return method;
    }

    /**
     * @return resource
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * @return resource class
     */
    public Class<?> getResourceType() {
        return resourceType;
    }

    /**
     * @return rest mapping url
     */
    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return method.getHttpMethod() + " " + url;
    }

    @Override
    public int compareTo(final ViewPath o) {
        return ComparisonChain.start()
                .compare(url, o.url)
                .compare(method.getHttpMethod(), o.method.getHttpMethod(), Comparator.nullsLast(Ordering.natural()))
                .result();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ViewPath)) {
            return false;
        }
        final ViewPath that = (ViewPath) o;
        return method.equals(that.method);
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }
}
