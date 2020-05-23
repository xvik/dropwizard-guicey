package ru.vyarus.dropwizard.guice.test.spock.ext;

import com.google.inject.Injector;
import com.google.inject.spi.InjectionPoint;
import io.dropwizard.testing.DropwizardTestSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import spock.lang.Shared;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * Leverages rules logic to start/stop application and injects Guice-provided objects into specifications.
 * <p>Implementation is very similar to original spock-guice module.</p>
 *
 * @author Vyacheslav Rusakov
 * @since 02.01.2015
 */
// Important implementation detail: Only the fixture methods of
// spec.getTopSpec() are intercepted (see GuiceExtension)
public class GuiceyInterceptor extends AbstractMethodInterceptor {

    private final EnvironmentSupport support;
    private final List<GuiceyConfigurationHook> hooks;
    private final Set<InjectionPoint> injectionPoints;
    private Injector injector;

    public GuiceyInterceptor(final SpecInfo spec, final EnvironmentSupport support,
                             final List<GuiceyConfigurationHook> hooks) {
        this.support = support;
        this.hooks = hooks;
        injectionPoints = InjectionPoint.forInstanceMethodsAndFields(spec.getReflection());
    }

    @Override
    public void interceptSharedInitializerMethod(final IMethodInvocation invocation) throws Throwable {
        hooks.forEach(GuiceyConfigurationHook::register);
        support.before();
        injector = support.getInjector();
        injectValues(invocation.getSharedInstance(), true);
        invocation.proceed();
    }

    @Override
    public void interceptInitializerMethod(final IMethodInvocation invocation) throws Throwable {
        injectValues(invocation.getInstance(), false);
        invocation.proceed();
    }

    @Override
    public void interceptCleanupSpecMethod(final IMethodInvocation invocation) throws Throwable {
        // just in case to avoid side-effects
        ConfigurationHooksSupport.reset();
        try {
            invocation.proceed();
        } finally {
            support.after();
        }
    }

    private void injectValues(final Object target, final boolean sharedFields) throws IllegalAccessException {
        for (InjectionPoint point : injectionPoints) {
            if (!(point.getMember() instanceof Field)) {
                throw new GuiceyExtensionException("Method injection is not supported; use field injection instead");
            }

            final Field field = (Field) point.getMember();
            if (field.isAnnotationPresent(Shared.class) != sharedFields) {
                continue;
            }

            final Object value = injector.getInstance(point.getDependencies().get(0).getKey());
            field.setAccessible(true);
            field.set(target, value);
        }
    }

    /**
     * External junit rules adapter.
     */
    public interface EnvironmentSupport {

        /**
         * Prepare environment.
         *
         * @throws Exception on error
         */
        void before() throws Exception;

        /**
         * Shutdown environment.
         *
         * @throws Exception on error
         */
        void after() throws Exception;

        /**
         * @return injector instance
         */
        Injector getInjector();
    }

    /**
     * Base environment support implementation. Used as-is for dropwizard test and requires advanced command
     * handling for guicey test (because dropwizard support will not properly shutdown it).
     */
    public abstract static class AbstractEnvironmentSupport implements EnvironmentSupport {
        private final Logger logger = LoggerFactory.getLogger(AbstractEnvironmentSupport.class);

        private final Class<?> test;
        private DropwizardTestSupport support;
        private ClientSupport client;

        public AbstractEnvironmentSupport(final Class<?> test) {
            this.test = test;
        }

        protected abstract DropwizardTestSupport build();

        @Override
        public void before() throws Exception {
            support = build();
            support.before();

            client = new ClientSupport(support);
            SpecialFieldsSupport.initClients(test, client);
        }

        @Override
        public void after() {
            if (support != null) {
                support.after();
            }
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    // not critical, just info
                    logger.info("Error closing client instance", e);
                }
            }
        }

        @Override
        public Injector getInjector() {
            return InjectorLookup.getInjector(support.getApplication())
                    .orElseThrow(() -> new IllegalStateException("No active injector found"));
        }
    }
}
