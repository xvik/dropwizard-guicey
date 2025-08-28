package ru.vyarus.dropwizard.guice.test.spy;

import com.google.common.base.Preconditions;
import com.google.inject.Provider;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * AOP interceptor redirect calls from the real bean into spy object, which was created around the same real bean.
 * <p>
 * There is a chicken-egg problem: service binding can't be overridden (with spy instance), because spy requires
 * service instance for construction. So, instead of replacing bean, we intercept bean calls. Actual spy object
 * is created lazily just after injector creation. On the first call, AOP interceptor breaks the current aop chain
 * (if other interceptors registered) and redirect calls to spy, which again calls the same service (including
 * aop handler), but, this time, it processes normally.
 *
 * @param <T> bean type
 */
public class SpyProxy<T> implements MethodInterceptor, Provider<T> {
    private final Class<T> type;
    private final List<Consumer<T>> startupInitializers = new ArrayList<>();
    private Provider<T> instanceProvider;
    private volatile T spy;

    /**
     * Create proxy.
     *
     * @param type bean type
     */
    public SpyProxy(final Class<T> type) {
        this.type = type;
    }

    /**
     * Actual spy object instance is created only on first bean access (after or in time of application startup).
     * Normally, it is ok to wait for application startup, configure spy object and then run tests methods (using
     * spy). But if spied bean is involved in application startup (called by some managed objects) then the only
     * way to configure it is to apply modification just after spy instance creation.
     * <p>
     * Might be called multiple times (for multiple initializers configuration).
     *
     * @param initializer spy object initializer
     * @return proxy instance
     */
    public final SpyProxy<T> withInitializer(final Consumer<T> initializer) {
        this.startupInitializers.add(initializer);
        return this;
    }

    /**
     * Delayed bean instance provider. Required because a proxy object created before guice modules processing
     * (provider could be obtained from binder).
     *
     * @param instanceProvider bean instance provider, used to get bean instance for spying
     */
    public void setInstanceProvider(final Provider<T> instanceProvider) {
        Preconditions.checkState(this.instanceProvider == null, "Instance provider already set");
        this.instanceProvider = instanceProvider;
    }

    /**
     * @return proxied bean type
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * @return spy instance
     */
    public synchronized T getSpy() {
        if (spy == null) {
            // lazy spy init
            final T bean = Preconditions.checkNotNull(instanceProvider.get());
            spy = Mockito.spy(bean);
            for (Consumer<T> initializer : startupInitializers) {
                initializer.accept(spy);
            }
        }
        return spy;
    }

    /**
     * Alternative spy provider to use proxy as {@code Provider<?>}. Internally, this method is not used as
     * it is hard to search usages for it.
     *
     * @return spy instance
     */
    @Override
    public T get() {
        return getSpy();
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public synchronized Object invoke(final MethodInvocation methodInvocation) throws Throwable {
        // WARNING: for proper execution, this requires this AOP handler to be top most!
        // (otherwise, some interceptors would be called multiple times)

        final boolean isSpyCalled = methodInvocation.getThis() == spy;
        if (isSpyCalled) {
            // second call (from spy) - normal execution, including all underlying aop
            return methodInvocation.proceed();
        }

        // first call - interceptor breaks the AOP chain by calling the same method on spy object, which
        // wraps the same proxied bean (so interceptor would be called second time)
        return methodInvocation.getMethod().invoke(getSpy(), methodInvocation.getArguments());
    }
}
