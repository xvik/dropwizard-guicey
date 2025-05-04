package ru.vyarus.dropwizard.guice.test.mock;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;

import java.util.HashMap;
import java.util.Map;

/**
 * Replace any guice service with mockito mock in test (using guice module overrides).
 * <p>
 * Important: requires mockito dependency!
 * <p>
 * Usage example:
 * <pre><code>
 *  MocksHook hook = new MocksHook();
 *  Service mock = hook.mock(Service.class);
 *  when(mock.foo()).thenReturn(..)
 * </code></pre>
 * <p>
 * Could also be used for spy objects registration of beans bound by instance (!)
 * (so {@link ru.vyarus.dropwizard.guice.test.spy.SpiesHook} could not be used):
 * {@code Service spy = hook.mock(Service.class, Mockito.spy(new Service()))}.
 * Spy should also be used when mock must be created from an abstract class (preserving abstract methods):
 * {@code AbstractService mock = hook.mock(AbstractService.class, Mockito.spy(AbstractService.class))}.
 * <p>
 * Limitation: any aop, applied to the original bean, will not work with mock (because guice can't apply aop to
 * instances)! Use {@link ru.vyarus.dropwizard.guice.test.spy.SpiesHook} instead if aop is important.
 * Does not work for HK2 beans.
 *
 * @author Vyacheslav Rusakov
 * @since 29.04.2025
 */
public class MocksHook implements GuiceyConfigurationHook {

    private final Map<Class<?>, Object> mocks = new HashMap<>();
    private boolean initialized;

    @Override
    public void configure(final GuiceBundle.Builder builder) throws Exception {
        if (!mocks.isEmpty()) {
            builder.modulesOverride(binder ->
                    mocks.forEach((aClass, o) -> bindMock(binder, aClass, o)));
        }
        initialized = true;
    }

    /**
     * Override gucie bean with a mock instance.
     *
     * @param type bean type
     * @param <T>  bean type
     * @return mock instance
     */
    public <T> T mock(final Class<T> type) {
        return mock(type, Mockito.mock(type));
    }

    /**
     * Override guice bean with a user-provided mock instance.
     *
     * @param type bean type
     * @param mock mock instance
     * @param <T>  bean type
     * @return passed mock instance
     */
    public <T> T mock(final Class<T> type, final T mock) {
        Preconditions.checkState(!initialized, "Too late. Mocks already applied.");
        Preconditions.checkState(MockUtil.isMock(mock), "Provided object is not a mockito mock object.");
        Preconditions.checkState(!mocks.containsKey(type), "Mock object for type %s is already registered.",
                type.getSimpleName());
        mocks.put(type, mock);
        return mock;
    }

    /**
     * @param type bean type
     * @param <T>  bean type
     * @return mock instance registered for bean type
     * @throws java.lang.IllegalStateException if mock for type is not registered
     */
    @SuppressWarnings("unchecked")
    public <T> T getMock(final Class<T> type) {
        return (T) Preconditions.checkNotNull(mocks.get(type), "Mock not registered for type %s", type.getSimpleName());
    }

    /**
     * Reset all registered mocks.
     */
    public void resetMocks() {
        mocks.values().forEach(Mockito::reset);
    }

    @SuppressWarnings("unchecked")
    private <K> void bindMock(final Binder binder, final Class<?> type, final Object mock) {
        binder.bind((Class<K>) type).toInstance((K) mock);
    }
}
