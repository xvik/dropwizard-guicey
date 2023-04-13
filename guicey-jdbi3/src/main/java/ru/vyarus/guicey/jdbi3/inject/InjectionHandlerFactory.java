package ru.vyarus.guicey.jdbi3.inject;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import org.jdbi.v3.core.extension.HandleSupplier;
import org.jdbi.v3.sqlobject.Handler;
import org.jdbi.v3.sqlobject.HandlerFactory;

import jakarta.inject.Inject;
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
public class InjectionHandlerFactory implements HandlerFactory {

    @Inject
    private Injector injector;

    @Override
    public Optional<Handler> buildHandler(final Class<?> sqlObjectType, final Method method) {
        if (method.getAnnotation(Inject.class) != null
                || method.getAnnotation(com.google.inject.Inject.class) != null) {
            return Optional.of(new InjectionHandler(injector, method.getReturnType()));
        }
        return Optional.empty();
    }

    /**
     * Handler provides guice managed instance on method call.
     */
    private static class InjectionHandler implements Handler {
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
        public Object invoke(final Object target,
                             final Object[] args,
                             final HandleSupplier handle) throws Exception {
            return injector.getInstance(type);
        }
    }
}
