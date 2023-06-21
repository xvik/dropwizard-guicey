package ru.vyarus.guicey.jdbi3.inject;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import jakarta.inject.Inject;
import org.jdbi.v3.core.extension.ExtensionHandler;
import org.jdbi.v3.core.extension.ExtensionHandlerFactory;
import org.jdbi.v3.core.extension.HandleSupplier;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Sql objects are forced to be interfaces now so it is impossible to inject guice bean (probably other proxy)
 * with field injection. In order to workaround this limitation getter injection must be used:
 * {@code @Inject MyBean getBean();}. Handler detects methods annotated with {@link Inject} or
 * {@link com.google.inject.Inject} and return actual guice bean on method call.
 *
 * @author Vyacheslav Rusakov
 * @since 17.09.2018
 */
public class InjectionHandlerFactory implements ExtensionHandlerFactory {

    @Inject
    private Injector injector;

    @Override
    public boolean accepts(final Class<?> extensionType, final Method method) {
        return method.getAnnotation(Inject.class) != null
                || method.getAnnotation(com.google.inject.Inject.class) != null;
    }

    @Override
    public Optional<ExtensionHandler> createExtensionHandler(final Class<?> extensionType, final Method method) {
        return Optional.of(new InjectionHandler(injector, method.getReturnType()));
    }

    /**
     * Handler provides guice managed instance on method call.
     */
    private static class InjectionHandler implements ExtensionHandler {
        private final Injector injector;
        private final Class<?> type;

        InjectionHandler(final Injector injector, final Class<?> type) {
            this.injector = Preconditions.checkNotNull(injector, "No injector");
            this.type = Preconditions.checkNotNull(type, "No type");
            Preconditions.checkState(type != Void.class && type != void.class,
                    "Only non void (getter) method could be anotated with @Inject in order"
                            + "to provide guice bean.");
        }

        @Override
        public Object invoke(final HandleSupplier handleSupplier,
                             final Object target,
                             final Object... args) throws Exception {
            return injector.getInstance(type);
        }
    }
}
