package ru.vyarus.dropwizard.guice.test.stub;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;

import java.util.HashMap;
import java.util.Map;

/**
 * Replace any guice service with its stub in test (using guice module overrides). Consider stubs as a hand-made
 * mocks.
 * <p>
 * Example: suppose we have some {@code Service} and we need to modify it for tests, so we extend it with
 * {@code class ServiceStub extends Service} and override required methods. Register stub in hook
 * as {@code hook.stub(Service.class, ServiceStub.class)}. Internally, overriding guice binding would be created:
 * {@code bind(Service.class).to(ServiceStub.class).in(Singleton.class)} so guice would
 * create stub instead of the original service. Guice would create stub instance, so injections would work inside it
 * (and AOP).
 * <p>
 * Stub could also be initialized manually: manual instance would be injected into guice context (annotated fields
 * injection would also be performed for provided instance): {@code hook.stub(Service.class, new ServiceStub())}.
 * <p>
 * More canonical example with interface: suppose we have {@code bind(IServie.clas).to(ServiceImpl.class))}. In this
 * case, stub could simply implement interface, instead of extending class
 * ({@code class ServiceStub implements IService}): {@code hook.stub(IService.class, ServiceStub.class)};
 * <p>
 * Just in case: guice injection will also return stabbed bean (because stub instance is created by guice or
 * instance bound into it).
 * <p>
 * Does not work for HK2 beans.
 *
 * @author Vyacheslav Rusakov
 * @since 30.04.2025
 */
public class StubsHook implements GuiceyConfigurationHook {

    private final Map<Class<?>, Object> stubs = new HashMap<>();
    // used for lifecycle support implementation in guice-managed stubs
    private final Map<Class<?>, Provider<?>> stubProviders = new HashMap<>();
    private boolean initialized;

    @Override
    public void configure(final GuiceBundle.Builder builder) throws Exception {
        if (!stubs.isEmpty()) {
            builder.modulesOverride(binder ->
                    stubs.forEach((aClass, o) -> bindStub(binder, aClass, o)));
        }
        initialized = true;
    }

    /**
     * Register stub class. Here stub must either extend original service ({@code class Stub extends Service}) or,
     * if target service use interface for binding ({@code bind(IService.class).to(ServiceImpl.class}), implement
     * that interface ({@code class Stub implements IService}).
     * <p>
     * Stub instance would be managed with guice and so guice AOP could be applied for stub.
     * <p>
     * If stub implements {@link ru.vyarus.dropwizard.guice.test.stub.StubLifecycle}, then stubs lifecycle could be
     * emulated with {@link #before()} and {@link #after()} methods. Might be used to reset stub state between tests.
     *
     * @param type overriding service type
     * @param stub stub implementation (used to override application service)
     * @param <T>  service type
     */
    public <T> void stub(final Class<T> type, final Class<? extends T> stub) {
        Preconditions.checkState(!initialized, "Too late. Spies already applied.");
        Preconditions.checkState(!type.equals(stub), "Stub must have a different type.");
        Preconditions.checkState(!stubs.containsKey(type), "Stub object for type %s is already registered.",
                type.getSimpleName());
        stubs.put(type, stub);
    }

    /**
     * Same as {@link #stub(Class, Class)}, but with manually created stub instance. In this case, guice AOP will not
     * work for sub instance. {@code Binder.requestInjection(stub)} would be called for stub instance to support
     * fields injection.
     *
     * @param type  overriding service type
     * @param value stub instance (used to override application service)
     * @param <T>   service type
     */
    public <T> void stub(final Class<T> type, final T value) {
        Preconditions.checkState(!initialized, "Too late. Spies already applied.");
        Preconditions.checkNotNull(value, "Stub cannot be null");
        Preconditions.checkState(!stubs.containsKey(type), "Stub object for type %s is already registered.",
                type.getSimpleName());
        stubs.put(type, value);
    }

    /**
     * Run {@link StubLifecycle#before()} for all stubs, implementing lifecycle interface.
     * For example, it could be called before each test.
     */
    public void before() {
        lifecycle(true);
    }

    /**
     * Run {@link StubLifecycle#after()} for all stubs, implementing lifecycle interface.
     * For example, it could be called after each test.
     */
    public void after() {
        lifecycle(false);
    }

    private void lifecycle(final boolean before) {
        stubs.forEach((type, o) -> {
            Object stub = o;
            if (o instanceof Class) {
                // instance managed by guice
                stub = stubProviders.get(type).get();
            }
            if (stub instanceof StubLifecycle) {
                final StubLifecycle lifecycle = (StubLifecycle) stub;
                if (before) {
                    lifecycle.before();
                } else {
                    lifecycle.after();
                }
            }
        });
    }

    /**
     * @param type bean type
     * @param <T>  bean type
     * @return stub instance registered for bean type
     * @throws java.lang.IllegalStateException if stub for type is not registered
     */
    @SuppressWarnings("unchecked")
    public <T, P extends T> P getStub(final Class<T> type) {
        return (P) Preconditions.checkNotNull(stubs.get(type), "Stub not registered for type %s", type.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private <K> void bindStub(final Binder binder, final Class<?> type, final Object stub) {
        if (stub instanceof Class) {
            // bind original type to stub - guice will instantiate it
            // IMPORTANT to bind as singleton - otherwise different instances would be everywhere
            binder.bind((Class<K>) type).to((Class<? extends K>) stub).in(Singleton.class);
            // store guice-managed instance accessor to apply lifecycle
            stubProviders.put(type, binder.getProvider(type));
        } else {
            binder.requestInjection(stub);
            binder.bind((Class<K>) type).toInstance((K) stub);
        }
    }
}
