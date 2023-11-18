package ru.vyarus.dropwizard.guice.test.jupiter.ext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.testing.DropwizardTestSupport;
import jakarta.inject.Qualifier;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.param.Jit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;

/**
 * Base class for junit 5 extensions. Supports direct injection of test parameters:
 * <ul>
 *     <li>{@link Application} or exact application class</li>
 *     <li>{@link ObjectMapper}</li>
 *     <li>{@link ClientSupport} application web client helper</li>
 *     <li>{@link DropwizardTestSupport} support object itself</li>
 *     <li>Any existing guice binding (possibly with qualifier annotation or generified)</li>
 *     <li>{@link Jit} annotated parameter will be obtained from guice context (assume JIT binding)</li>
 * </ul>
 * Overall, it provides everything {@link DropwizardTestSupport} provides plus guice-managed beans.
 *
 * @author Vyacheslav Rusakov
 * @since 29.04.2020
 */
public abstract class TestParametersSupport implements ParameterResolver {

    private final List<Class<?>> supportedClasses = ImmutableList.of(
            ObjectMapper.class,
            ClientSupport.class,
            DropwizardTestSupport.class);

    @Override
    @SuppressWarnings("checkstyle:ReturnCount")
    public boolean supportsParameter(final ParameterContext parameterContext,
                                     final ExtensionContext extensionContext) throws ParameterResolutionException {
        final Parameter parameter = parameterContext.getParameter();
        if (parameter.getAnnotations().length > 0) {
            if (AnnotationSupport.isAnnotated(parameter, Jit.class)) {
                return true;
            } else if (!isQualifierAnnotation(parameter.getAnnotations())) {
                // if any other annotation declared on the parameter - skip it (possibly other extension's parameter)
                return false;
            }
        }

        final Class<?> type = parameter.getType();
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
        return getInjector(extensionContext)
                .map(it -> it.getExistingBinding(getKey(parameter)) != null)
                .orElse(false);
    }

    @Override
    @SuppressWarnings("checkstyle:ReturnCount")
    public Object resolveParameter(final ParameterContext parameterContext,
                                   final ExtensionContext extensionContext) throws ParameterResolutionException {
        final Parameter parameter = parameterContext.getParameter();
        final Class<?> type = parameter.getType();
        if (ClientSupport.class.equals(type)) {
            return Preconditions.checkNotNull(getClient(extensionContext), "ClientSupport object not available");
        }
        final DropwizardTestSupport<?> support = Preconditions.checkNotNull(getSupport(extensionContext));
        if (Application.class.isAssignableFrom(type)) {
            return support.getApplication();
        }
        if (ObjectMapper.class.equals(type)) {
            return support.getObjectMapper();
        }
        if (DropwizardTestSupport.class.isAssignableFrom(type)) {
            return support;
        }
        return InjectorLookup.getInjector(support.getApplication())
                .map(it -> it.getInstance(getKey(parameter)))
                .get();
    }

    /**
     * @param extensionContext junit extension context
     * @return dropwizard test support object assigned to test instance or null
     */
    protected abstract DropwizardTestSupport<?> getSupport(ExtensionContext extensionContext);

    /**
     * @param extensionContext junit extension context
     * @return client factory object assigned to test instance or null
     */
    protected abstract ClientSupport getClient(ExtensionContext extensionContext);

    /**
     * @param extensionContext junit extension context
     * @return application injector or null
     */
    protected abstract Optional<Injector> getInjector(ExtensionContext extensionContext);

    private boolean isQualifierAnnotation(final Annotation... annotations) {
        final Annotation ann = annotations[0];
        return annotations.length == 1
                && (AnnotationSupport.isAnnotated(ann.annotationType(), Qualifier.class)
                || AnnotationSupport.isAnnotated(ann.annotationType(), BindingAnnotation.class));
    }

    private Key<?> getKey(final Parameter parameter) {
        final Key<?> key;
        if (parameter.getAnnotations().length > 0
                && !AnnotationSupport.isAnnotated(parameter, Jit.class)) {
            // qualified bean
            key = Key.get(parameter.getParameterizedType(), parameter.getAnnotations()[0]);
        } else {
            key = Key.get(parameter.getParameterizedType());
        }
        return key;
    }
}
