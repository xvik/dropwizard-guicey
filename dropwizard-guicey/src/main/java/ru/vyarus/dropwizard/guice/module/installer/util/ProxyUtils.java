package ru.vyarus.dropwizard.guice.module.installer.util;

import com.google.common.base.Preconditions;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Utility to create class proxies (where all method calls intercepted with a custom handler).
 *
 * @author Vyacheslav Rusakov
 * @since 14.12.2025
 */
public final class ProxyUtils {

    private ProxyUtils() {
    }

    /**
     * Create proxy instance for class or interface.
     * <p>
     * If the source class has no no-arg constructor, declared constructor with minimal arguments would be used
     * (with nulls or dummy arguments).
     *
     * @param source  class to create proxy for
     * @param handler methods handler
     * @param <T>     class type
     * @return created proxy instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(final Class<T> source, final MethodHandler handler) {
        Preconditions.checkArgument(source != null, "Proxied class is required");
        final ProxyFactory factory = new ProxyFactory();
        if (source.isInterface()) {
            factory.setInterfaces(new Class[]{source});
        } else {
            factory.setSuperclass(source);
        }
        // create instance either with default constructor or constructor with minimal parameters
        // (using nulls)
        final T proxy;
        try {
            proxy = InstanceUtils.createWithAnyConstructor((Class<T>) factory.createClass());
        } catch (Exception e) {
            // human-readable error
            throw new IllegalStateException("Failed to create proxy for class " + source.getName(), e);
        }

        ((Proxy) proxy).setHandler(handler);
        return proxy;
    }

    /**
     * Create a dummy class proxy (like a mock): all methods would return dummy objects.
     * <p>
     * Such a proxy might be required, for example, for abstract constructor arguments when dummy argument
     * must be created.
     *
     * @param source class to create proxy for
     * @param <T>    class type
     * @return dummy proxy instance
     */
    public static <T> T createDummyProxy(final Class<T> source) {
        return createProxy(source, new DummyHandler(source));
    }

    /**
     * Dummy handler used for abstract classes instantiation. Returns "mocked" results for all methods
     * (to prevent NPE).
     */
    public static class DummyHandler implements MethodHandler {
        private final Class<?> type;

        /**
         * Create dummy method handler.
         * 
         * @param type proxied class
         */
        public DummyHandler(final Class<?> type) {
            this.type = type;
        }

        @Override
        @SuppressWarnings("checkstyle:ReturnCount")
        public Object invoke(final Object self,
                             final Method thisMethod,
                             final Method proceed,
                             final Object[] args) throws Throwable {
            final Class<?> returnType = thisMethod.getReturnType();
            if (returnType == Void.TYPE) {
                return null;
            } else {
                if ("toString".equals(thisMethod.getName())) {
                    // important to easily differentiate such objects in debugger
                    return type.getSimpleName() + " dummy proxy";
                } else if ("equals".equals(thisMethod.getName())) {
                    return Objects.equals(self, args[0]);
                } else if ("hashCode".equals(thisMethod.getName())) {
                    return Objects.hashCode(self);
                }
                // return non-null instance
                return InstanceUtils.createDummyInstance(returnType);
            }
        }
    }
}
