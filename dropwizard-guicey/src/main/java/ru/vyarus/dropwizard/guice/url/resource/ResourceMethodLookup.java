package ru.vyarus.dropwizard.guice.url.resource;

import com.google.common.base.Preconditions;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.module.installer.util.ProxyUtils;
import ru.vyarus.dropwizard.guice.url.model.MethodCall;
import ru.vyarus.dropwizard.guice.url.util.Caller;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utility for intercepting resource call (on special proxy object) to extract called method and provided arguments.
 * This is useful for simplifying target method path building (based on jersey annotations).
 *
 * @author Vyacheslav Rusakov
 * @since 25.09.2025
 */
public final class ResourceMethodLookup {

    private ResourceMethodLookup() {
    }

    /**
     * Procide special proxy object into consumer in order to intercept method call and record target method
     * with called arguments.
     *
     * @param resource resource method
     * @param call     consumer with method(s) call
     * @param <T>      resource type
     * @return called methods
     */
    public static <T> List<MethodCall> getMethodCalls(final Class<T> resource, final Caller<T> call) {
        final T proxy = createLookupProxy(resource, null);
        try {
            call.call(proxy);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Failed to record method call on resource '%s'",
                    resource.getSimpleName()), e);
        }
        return getMethodCalls(proxy);
    }

    /**
     * Extract recorded method calls from proxy. Throw exception if no methods were called.
     *
     * @param proxy proxy instance created by {@link #createLookupProxy(Class, java.util.function.Consumer)}
     * @return list of called methods
     */
    private static List<MethodCall> getMethodCalls(final Object proxy) {
        return ((Handler) ((ProxyObject) proxy).getHandler()).getCalls();
    }

    /**
     * Create a proxy from a resource class. Proxy would record all method calls (without calling actual class methods).
     * Use {@link #getMethodCalls(Object)} to get recorded calls.
     *
     * @param resource    resource class
     * @param callHandler optional direct call handler, used for sub-method calls processing
     * @param <T>         resource type
     * @return proxy instance
     */
    private static <T> T createLookupProxy(final Class<T> resource, final Consumer<MethodCall> callHandler) {
        final Handler handler = new Handler(resource, callHandler);
        return ProxyUtils.createProxy(resource, handler);
    }

    /**
     * Handler, intercepting proxy method calls.
     */
    public static class Handler implements MethodHandler {
        private final Class<?> resource;
        private final List<MethodCall> calls = new java.util.ArrayList<>();
        private final Consumer<MethodCall> subCallHandler;

        /**
         * Create a method interceptor.
         *
         * @param resource       resource class
         * @param subCallHandler optional direct call handler, used for sub-method calls processing
         */
        public Handler(final Class<?> resource, final @Nullable Consumer<MethodCall> subCallHandler) {
            this.resource = resource;
            this.subCallHandler = subCallHandler;
        }

        @Override
        @SuppressWarnings("checkstyle:ReturnCount")
        public Object invoke(final Object self, final Method thisMethod, final Method proceed,
                             final Object[] args) throws Throwable {
            final MethodCall call = new MethodCall(resource, thisMethod, args);
            calls.add(call);

            // handling sub-method call with direct listener
            if (subCallHandler != null) {
                subCallHandler.accept(call);
            }

            if (!thisMethod.getReturnType().equals(void.class)
                    && !thisMethod.getReturnType().getPackageName().startsWith("java.")
                    && !thisMethod.getReturnType().getPackageName().startsWith("jakarta.")) {
                return createLookupProxy(thisMethod.getReturnType(), call::setSubCall);
            }
            if (thisMethod.getReturnType().isPrimitive()) {
                // required to avoid null pointer exceptions
                return createPrimitive(thisMethod.getReturnType());
            }
            // just record call, no actual method execution required
            return null;
        }

        // hack required to correctly handle methods with primitive return type
        @SuppressWarnings("checkstyle:ReturnCount")
        private Object createPrimitive(final Class<?> type) {
            if (type == byte.class) {
                return (byte) 0;
            } else if (type == long.class) {
                return (long) 0;
            } else if (type == short.class) {
                return (short) 0;
            } else if (type == int.class) {
                return 0;
            } else if (type == float.class) {
                return (float) 0;
            } else if (type == double.class) {
                return (double) 0;
            } else if (type == boolean.class) {
                return false;
            } else if (type == char.class) {
                return '\0';
            }
            return 0;
        }

        /**
         * @return recorded calls
         * @throws java.lang.IllegalStateException if nothing recorded
         */
        public List<MethodCall> getCalls() {
            Preconditions.checkState(!calls.isEmpty(), "No method calls recorded");
            return calls;
        }
    }
}
