package ru.vyarus.dropwizard.guice.test.spock.ext;

import com.google.inject.Injector;
import com.google.inject.spi.InjectionPoint;
import org.junit.rules.ExternalResource;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;
import ru.vyarus.dropwizard.guice.module.support.conf.ConfiguratorsSupport;
import ru.vyarus.dropwizard.guice.module.support.conf.GuiceyConfigurator;
import spock.lang.Shared;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    private static Method before;
    private static Method after;

    private final ExternalRuleAdapter externalRuleAdapter;
    private final List<GuiceyConfigurator> configurators;
    private final Set<InjectionPoint> injectionPoints;
    private ExternalResource resource;

    static {
        // resolve methods eagerly to speedup execution
        try {
            before = ExternalResource.class.getDeclaredMethod("before");
            before.setAccessible(true);
            after = ExternalResource.class.getDeclaredMethod("after");
            after.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new GuiceyExtensionException("Failed resolve method", e);
        }
    }

    public GuiceyInterceptor(final SpecInfo spec, final ExternalRuleAdapter externalRuleAdapter,
                             final List<GuiceyConfigurator> configurators) {
        this.externalRuleAdapter = externalRuleAdapter;
        this.configurators = configurators;
        injectionPoints = InjectionPoint.forInstanceMethodsAndFields(spec.getReflection());
    }

    @Override
    public void interceptSharedInitializerMethod(final IMethodInvocation invocation) throws Throwable {
        configurators.forEach(ConfiguratorsSupport::listen);
        if (resource == null) {
            resource = externalRuleAdapter.newResource();
        }
        before.invoke(resource);
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
        ConfiguratorsSupport.reset();
        try {
            invocation.proceed();
        } finally {
            after.invoke(resource);
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

            final Object value = externalRuleAdapter.getInjector().getInstance(point.getDependencies().get(0).getKey());
            field.setAccessible(true);
            field.set(target, value);
        }
    }

    /**
     * External junit rules adapter.
     */
    public interface ExternalRuleAdapter {

        /**
         * @return new rule instance
         */
        ExternalResource newResource();

        /**
         * @return injector instance
         */
        Injector getInjector();
    }
}
