package ru.vyarus.dropwizard.guice.test.jupiter.ext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Key;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.test.jupiter.param.AppAdminPort;
import ru.vyarus.dropwizard.guice.test.jupiter.param.AppPort;
import ru.vyarus.dropwizard.guice.test.jupiter.param.Jit;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Base class for junit 5 extensions. Supports direct injection of test parameters:
 * <ul>
 *     <li>{@link Application} class or exact application class</li>
 *     <li>{@link Configuration} class or exact configuration class</li>
 *     <li>{@link Environment} and {@link ObjectMapper}</li>
 *     <li>{@link com.google.inject.Injector}</li>
 *     <li>Any existing guice binging (without qualifiers)</li>
 *     <li>{@link Jit} annotated parameter will be obtained from guice context (assume JIT binding)</li>
 *     <li>{@code int} parameter annotated with {@link AppPort} or {@link AppAdminPort}</li>
 * </ul>
 * Overall, it provides everything {@link DropwizardTestSupport} provides plus guice-managed beans.
 *
 * @author Vyacheslav Rusakov
 * @since 29.04.2020
 */
public abstract class TestParametersSupport implements ParameterResolver {

    private final List<Class<?>> supportedClasses = ImmutableList.of(
            Application.class,
            Configuration.class,
            Environment.class,
            ObjectMapper.class,
            Injector.class);

    private final List<Class<? extends Annotation>> supportedAnnotations = ImmutableList.of(
            Jit.class,
            AppPort.class,
            AppAdminPort.class);

    @Override
    @SuppressWarnings("checkstyle:ReturnCount")
    public boolean supportsParameter(final ParameterContext parameterContext,
                                     final ExtensionContext extensionContext) throws ParameterResolutionException {
        final Class<?> type = parameterContext.getParameter().getType();
        if (parameterContext.getParameter().getAnnotations().length > 0) {
            final Class<? extends Annotation> ann = findSupportedAnnotation(parameterContext);
            // annotated parameter support
            if (ann != null && (ann.equals(AppPort.class) || ann.equals(AppAdminPort.class))) {
                Preconditions.checkState(type.equals(int.class),
                        "Port parameter annotated with @%s must be with type 'int'", ann.getSimpleName());
            }
            // if any other (not supported) annotation declared on the parameter - skip it
            // (possibly other extension's parameter)
            return ann != null;
        }

        if (Application.class.isAssignableFrom(type) || Configuration.class.isAssignableFrom(type)) {
            // special case when exact app or configuration class used
            return true;
        } else {
            for (Class<?> cls : supportedClasses) {
                if (type.equals(cls)) {
                    return true;
                }
            }
        }

        // declared guice binding (by class only)
        final DropwizardTestSupport<?> support = Preconditions.checkNotNull(getSupport(extensionContext));

        return InjectorLookup.getInjector(support.getApplication())
                .map(it -> it.getExistingBinding(Key.get(type)) != null)
                .orElse(false);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext,
                                   final ExtensionContext extensionContext) throws ParameterResolutionException {
        final DropwizardTestSupport<?> support = Preconditions.checkNotNull(getSupport(extensionContext));
        final Class<?> type = parameterContext.getParameter().getType();
        final Class<? extends Annotation> ann = findSupportedAnnotation(parameterContext);
        if (ann != null) {
            return lookupAnnotatedParam(ann, type, support);
        }
        return lookupParam(type, support);
    }

    /**
     * @param extensionContext junit extension context
     * @return dropwizard test support object assigned to test instance
     */
    protected abstract DropwizardTestSupport<?> getSupport(ExtensionContext extensionContext);

    private Class<? extends Annotation> findSupportedAnnotation(final ParameterContext parameterContext) {
        for (Class<? extends Annotation> ann : supportedAnnotations) {
            if (parameterContext.isAnnotated(ann)) {
                return ann;
            }
        }
        return null;
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    private Object lookupAnnotatedParam(final Class<? extends Annotation> ann,
                                        final Class<?> type,
                                        final DropwizardTestSupport<?> support) {
        if (ann.equals(AppPort.class)) {
            // may fail if web part not started
            return support.getLocalPort();
        }
        if (ann.equals(AppAdminPort.class)) {
            // may fail if web part not started
            return support.getAdminPort();
        }
        // injector MAY be null (in command tests)
        return InjectorLookup.getInjector(support.getApplication())
                .map(it -> it.getInstance(type))
                .orElse(null);
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    private Object lookupParam(final Class<?> type, final DropwizardTestSupport<?> support) {
        if (Application.class.isAssignableFrom(type)) {
            return support.getApplication();
        }
        if (Configuration.class.isAssignableFrom(type)) {
            return support.getConfiguration();
        }
        if (Environment.class.equals(type)) {
            return support.getEnvironment();
        }
        if (ObjectMapper.class.equals(type)) {
            return support.getObjectMapper();
        }
        if (Injector.class.equals(type)) {
            // injector MAY be null (in command tests)
            return InjectorLookup.getInjector(support.getApplication()).orElse(null);
        }

        return InjectorLookup.getInjector(support.getApplication()).map(it -> it.getInstance(type)).orElse(null);
    }
}
