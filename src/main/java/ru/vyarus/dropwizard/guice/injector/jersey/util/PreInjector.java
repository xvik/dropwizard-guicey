package ru.vyarus.dropwizard.guice.injector.jersey.util;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * During jersey startup, it relies on fields injection into existing instances (with
 * {@link org.glassfish.jersey.internal.inject.InjectionManager#inject(Object)}). In order to support this,
 * manual injection fields detection is used. But inly services, already registered as instances could be injected
 * this way.
 *
 * @author Vyacheslav Rusakov
 * @since 26.04.2019
 */
public class PreInjector {
    private Logger logger = LoggerFactory.getLogger(PreInjector.class);

    private Map<Class<?>, Object> knownServices = new HashMap<>();
    private Supplier<Injector> injector;

    public PreInjector(Supplier<Injector> injector) {
        this.injector = injector;
    }

    public void register(Object service) {
        Class<?> key = service.getClass();
        final Object registered = knownServices.get(key);
        // one service could be registered multiple times in jersey (to specify different contract)
        if (registered !=null && registered != service) {
            throw new IllegalArgumentException("Instance of this type was already registered: "+key.getName());
        }
        knownServices.put(key, service);
    }

    // todo remember this instance and bind to context later
    public Object create(Class type) {
        logger.debug("Perform MANUAL instance creation for {}", type.getName());
        // assume no-args constructors only
        try {
            Object res = type.newInstance();
            inject(res);
            return res;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create "+type.getName(), e);
        }
    }

    public void inject(Object service) {
        Class<?> key = service.getClass();
        logger.debug("Performing PRE INJECTION for {}", key.getName());
        Class<?> curr = key;
        while (curr != Object.class) {
            for(Field field: key.getDeclaredFields()) {
                // todo search for qualifiers?
                if (field.isAnnotationPresent(Inject.class)) {
                    logger.debug("Found @Inject {}.{}", key.getSimpleName(), field.getName());
                    Class<?> injectionPoint = field.getType();
                    Object inject = findService(injectionPoint);
                    if (inject == null) {
                        throw new IllegalStateException("Failed to perform injection for "+key.getName()+"."+field.getName()+": no service found for injection");
                    }

                     inject(service, field, inject, key);
                } else if (field.isAnnotationPresent(Context.class)) {
                    logger.debug("Found @Context {}.{}", key.getSimpleName(), field.getName());
                    // dynamic proxy injection
                    Object inject = ContextProxyFactory.create(injector, field.getType());
                    inject(service, field, inject, key);
                }
            }
            curr = curr.getSuperclass();
        }
    }

    private Object findService(Class<?> service) {
        for(Map.Entry<Class<?>, Object> entry: knownServices.entrySet()) {
            Class<?> decl = entry.getKey();
            if (service.isAssignableFrom(decl)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void inject(Object service, Field field, Object value, Class root) {
        boolean acces = field.isAccessible();
        try {
            field.setAccessible(true);
            field.set(service, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to perform injection for "+root.getName()+"."+field.getName(), e);
        } finally {
            field.setAccessible(acces);
        }
    }
}
