package ru.vyarus.dropwizard.guice.test.spock.ext;

import com.google.inject.spi.InjectionPoint;
import org.junit.rules.ExternalResource;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import spock.lang.Shared;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    private final ResourceFactory resourceFactory;
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

    public GuiceyInterceptor(final SpecInfo spec, final ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
        injectionPoints = InjectionPoint.forInstanceMethodsAndFields(spec.getReflection());
    }

    @Override
    public void interceptSharedInitializerMethod(final IMethodInvocation invocation) throws Throwable {
        if (resource == null) {
            resource = resourceFactory.newResource();
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

            final Object value = GuiceBundle.getInjector().getInstance(point.getDependencies().get(0).getKey());
            field.setAccessible(true);
            field.set(target, value);
        }
    }

    /**
     * Resource factory used to create rule instance.
     */
    public interface ResourceFactory {

        /**
         * @return new rule instance
         */
        ExternalResource newResource();
    }
}
