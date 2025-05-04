package ru.vyarus.dropwizard.guice.test.spy;

import com.google.common.base.Preconditions;
import com.google.inject.matcher.Matchers;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;

import java.util.HashMap;
import java.util.Map;

/**
 * Replace any guice service with mockito spy. The difference with mock: spy wraps around the real service(!)
 * and could be used to validate called service methods (verify incoming parameters and output value).
 * <p>
 * Important: requires mockito dependency!
 * <p>
 * In contrast to mocks and stubs, spies work with guice AOP: all calls to service are intercepted and
 * passed through the spy object (as "proxy"). That also means that all aop, applied to the original bean, would
 * work (in contrast to mocks).
 * <p>
 * As spy requires real bean instance - spy object is created just after injector creation (and AOP interceptor
 * redirects into it (then real bean called)). Spy object is different instance as injected bean
 * ({@code @Inject SpiedService}) because injected bean would be a proxy, handling guice AOP.
 * <p>
 * Calling bean methods directly on spy is completely normal (guice bean just redirects calls to spy object)!
 * <p>
 * Usage example:
 * <pre><code>
 *     SpiesHook hook = new SpiesHook();
 *     SpyProxy&lt;Service> proxy = hook.spy(Service.class)
 *     // actual spy object can be obtained only after guice application startup
 *     Service spy = proxy.getSpy()
 *     doReturn(12).when(spy).foo();
 * </code></pre>
 * <p>
 * Alternatively, provider might be used instead of proxy type (for simplicity):
 * {@code Provider<Service> provider = hook.spy(Service.class)}
 * <p>
 * Spy CAN'T be initialized manually. Use {@link ru.vyarus.dropwizard.guice.test.mock.MocksHook}
 * or {@link ru.vyarus.dropwizard.guice.test.stub.StubsHook}
 * instead for manual spy objects initialization. Such manual initialization could be required for spies created
 * from abstract classes (or multiple interfaces): in this case actual bean instance is not required, and so mocks
 * support could be used instead:
 * {@code AbstractService spy = mocksHook.mock(AbstractService.class, Mockito.spy(AbstractService.class))}.
 * <p>
 * Actual spy object instance is created only on first bean access (after or in time of application startup).
 * Normally, it is ok to wait for application startup, configure spy object and then run tests methods (using
 * spy). But if spied bean is involved in application startup (called by some managed objects) then the only
 * way to configure it is to apply modification just after spy instance creation:
 * {@code hook.spy(Service.class).withInitializer(spy -> { doReturn(12).when(spy).foo() })}.
 *
 * @author Vyacheslav Rusakov
 * @since 29.04.2025
 */
public class SpiesHook implements GuiceyConfigurationHook {

    private final Map<Class<?>, SpyProxy> spies = new HashMap<>();
    private boolean initialized;

    @Override
    @SuppressWarnings("unchecked")
    public void configure(final GuiceBundle.Builder builder) throws Exception {
        if (!spies.isEmpty()) {
            builder.modulesOverride(binder -> {
                spies.forEach((type, spy) -> {
                    spy.setInstanceProvider(binder.getProvider(type));
                    // real binding isn't overridden, just used aop to intercept call and redirect into spy
                    binder.bindInterceptor(Matchers.only(type), Matchers.any(), spy);
                });
            });
        }
        initialized = true;
    }

    /**
     * Request wrapping target bean with a spy.
     * <p>
     * As spy object requires bean instance, then spy could be created only during injector startup. Returned proxy
     * must be used to get an actual spy object after application startup (for configuration before test logic
     * execution).
     * <p>
     * Returned proxy instance could be used for startup initializer registration:
     * {@code hook.spy(Service.class).withInitializer(...)} (required ONLY in cases when spy must be used during
     * application startup and so can't be configured after application startup.
     * <p>
     * Returned proxy also implements {@code Provider} interface, so provider could be used instead of proxy type:
     * {@code Provider<Service> provider = hook.spy(Service.class)}
     * <p>
     * For spies targeting guice beans registered by instance use mocks
     * ({@link ru.vyarus.dropwizard.guice.test.mock.MocksHook}) because in this case bean instance is not required.
     *
     * @param type bean type
     * @return spy proxy instance
     * @param <T> bean type
     */
    public <T> SpyProxy<T> spy(final Class<T> type) {
        Preconditions.checkState(!initialized, "Too late. Spies already applied.");
        Preconditions.checkState(!spies.containsKey(type), "Mock object for type %s is already registered.",
                type.getSimpleName());
        final SpyProxy<T> spy = new SpyProxy<>(type);
        spies.put(type, spy);
        return spy;
    }

    /**
     * @param type bean type
     * @return mockito spy object (not proxy!)
     * @param <T>  bean type
     * @throws java.lang.IllegalStateException if spy for bean is not registered
     */
    @SuppressWarnings("unchecked")
    public <T> T getSpy(final Class<T> type) {
        return (T) Preconditions.checkNotNull(
                spies.get(type), "Spy not registered for type %s", type.getSimpleName())
                .getSpy();
    }

    /**
     * Reset all registered spies.
     */
    public void resetSpies() {
        spies.values().forEach(spyProxy -> Mockito.reset(spyProxy.getSpy()));
    }
}
