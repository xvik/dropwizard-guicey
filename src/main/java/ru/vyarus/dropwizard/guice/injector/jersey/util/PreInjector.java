package ru.vyarus.dropwizard.guice.injector.jersey.util;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    // manually created instances (created objects must be bound!)
    private Map<Class<?>, Object> instantiated = new HashMap<>();
    // to avoid duplicate injection
    private Set<Class<?>> membersInjected = new HashSet<>();

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

    public Object create(Class type) {
        Object res =  knownServices.get(type);
        if (res == null) {
            logger.debug("Perform MANUAL instance creation for {}", type.getName());
            // assume no-args constructors only
            try {
                res = type.newInstance();
                knownServices.put(type, res);
                instantiated.put(type, res);

            } catch (Exception e) {
                throw new IllegalStateException("Failed to create " + type.getName(), e);
            }
        }
        // inject members only once
        if (!membersInjected.contains(type)) {
            injectMembers(res);
            membersInjected.add(type);
        }
        return res;
    }
    public boolean isManuallyInstantiated(Class<?> type) {
        return instantiated.containsKey(type);
    }

    public Object getManualInstance(Class<?> type) {
        return instantiated.get(type);
    }

    public void injectMembers(Object service) {
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

                     injectMember(service, field, inject, key);
                } else if (field.isAnnotationPresent(Context.class)) {
                    logger.debug("Found @Context {}.{}", key.getSimpleName(), field.getName());
                    // dynamic proxy injection
                    Object inject = ContextProxyFactory.create(injector, field.getType());
                    injectMember(service, field, inject, key);
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

    private void injectMember(Object service, Field field, Object value, Class root) {
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
