package ru.vyarus.dropwizard.guice.url.model;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Recorded method call on resource class. The call may include sub-resource calls like:
 * {@code resource.subResource(args).method(args)}. In this case the root would be a sub-resource method call,
 * and a subCall would be a method call on sub-resource (there might be multiple sib-resource calls).
 *
 * @author Vyacheslav Rusakov
 * @since 25.09.2025
 */
public class MethodCall {
    private final Class<?> resource;
    private final Method method;
    private final Object[] args;
    private MethodCall subCall;

    /**
     * Create a method call info.
     *
     * @param resource resource type
     * @param method   called method
     * @param args     call arguments
     */
    public MethodCall(final Class<?> resource, final Method method, final Object... args) {
        this.resource = resource;
        this.method = method;
        this.args = args;
    }

    /**
     * @return resource class where method was called
     */
    public Class<?> getResource() {
        return resource;
    }

    /**
     * @return method instance
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return call arguments
     */
    @SuppressWarnings("PMD.MethodReturnsInternalArray")
    public Object[] getArgs() {
        return args;
    }

    /**
     * @return sub method call
     */
    @Nullable
    public MethodCall getSubCall() {
        return subCall;
    }

    /**
     * Set sub method call (call chain case).
     *
     * @param subCall sub method call
     */
    public void setSubCall(final MethodCall subCall) {
        this.subCall = subCall;
    }
}
