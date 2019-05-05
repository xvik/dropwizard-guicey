package ru.vyarus.dropwizard.guice.injector.jersey.util;

import com.google.inject.Injector;
import com.google.inject.Key;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import ru.vyarus.java.generics.resolver.util.GenericsUtils;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;
import ru.vyarus.java.generics.resolver.util.map.EmptyGenericsMap;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Supplier;

/**
 * @author Vyacheslav Rusakov
 * @since 27.04.2019
 */
public class ContextProxyFactory {

    public static Object create(Supplier<Injector> injector, Type target) {
        ProxyFactory factory = new ProxyFactory();
        final Class<?> clazz = GenericsUtils.resolveClass(target, EmptyGenericsMap.getInstance());
        if (clazz.isInterface()) {
            factory.setInterfaces(new Class[]{clazz});
        } else {
            factory.setSuperclass(clazz);
        }
        Class proxy = factory.createClass();
        MethodHandler handler = (self, overridden, forwarder, args) -> {
            Object instance = injector.get().getInstance(Key.get(target));
            return forwarder.invoke(instance, args);
        };
        Object instance;
        try {
            instance = proxy.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create @Context injection proxy for "
                    + TypeToStringUtils.toStringType(target, EmptyGenericsMap.getInstance()), e);
        }
        ((ProxyObject) instance).setHandler(handler);
        return instance;
    }
}
