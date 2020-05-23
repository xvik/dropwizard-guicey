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
import org.junit.platform.commons.support.AnnotationSupport;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.param.Jit;

import java.util.List;
import java.util.Optional;

/**
 * Base class for junit 5 extensions. Supports direct injection of test parameters:
 * <ul>
 *     <li>{@link Application} class or exact application class</li>
 *     <li>{@link Configuration} class or exact configuration class</li>
 *     <li>{@link Environment} and {@link ObjectMapper}</li>
 *     <li>{@link com.google.inject.Injector}</li>
 *     <li>{@link ClientSupport} application web client helper</li>
 *     <li>Any existing guice binging (without qualifiers)</li>
 *     <li>{@link Jit} annotated parameter will be obtained from guice context (assume JIT binding)</li>
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
            Injector.class,
            ClientSupport.class);

    @Override
    @SuppressWarnings("checkstyle:ReturnCount")
    public boolean supportsParameter(final ParameterContext parameterContext,
                                     final ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getAnnotations().length > 0) {
            // if any other annotation declared on the parameter - skip it (possibly other extension's parameter)
            return AnnotationSupport.isAnnotated(parameterContext.getParameter(), Jit.class);
        }

        final Class<?> type = parameterContext.getParameter().getType();
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
                .map(it -> it.getExistingBinding(Key.get(type)) != null)
                .orElse(false);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext,
                                   final ExtensionContext extensionContext) throws ParameterResolutionException {
        final Class<?> type = parameterContext.getParameter().getType();
        if (AnnotationSupport.isAnnotated(parameterContext.getParameter(), Jit.class)) {
            return getInjector(extensionContext).map(it -> it.getInstance(type)).get();
        }
        final DropwizardTestSupport<?> support = Preconditions.checkNotNull(getSupport(extensionContext));
        return ClientSupport.class.equals(type) ? getClient(extensionContext) : lookupParam(type, support);
    }

    /**
     * @param extensionContext junit extension context
     * @return dropwizard test support object assigned to test instance or null
     */
    protected abstract DropwizardTestSupport<?> getSupport(ExtensionContext extensionContext);

    /**
     * @param extensionContext junit extension context
     * @return client factory object assigned to test instance (never null)
     */
    protected abstract ClientSupport getClient(ExtensionContext extensionContext);

    /**
     * @param extensionContext junit extension context
     * @return application injector or null
     */
    protected abstract Optional<Injector> getInjector(ExtensionContext extensionContext);

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
        if (ClientSupport.class.equals(type)) {
            return new ClientSupport(support);
        }
        if (Injector.class.equals(type)) {
            return InjectorLookup.getInjector(support.getApplication()).get();
        }

        return InjectorLookup.getInjector(support.getApplication()).map(it -> it.getInstance(type)).get();
    }
}
