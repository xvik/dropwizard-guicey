package ru.vyarus.dropwizard.guice.injector.jersey.context;

import com.google.inject.Injector;
import com.google.inject.Key;
import org.glassfish.jersey.internal.inject.*;
import ru.vyarus.java.generics.resolver.GenericsResolver;
import ru.vyarus.java.generics.resolver.util.GenericsUtils;
import ru.vyarus.java.generics.resolver.util.map.EmptyGenericsMap;

import javax.inject.Provider;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Supplier;

/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2019
 */
public class GuiceContextInjectionResolver implements ContextInjectionResolver {

    Supplier<Injector> injector;

    public GuiceContextInjectionResolver(Supplier<Injector> injector) {
        this.injector = injector;
    }

    @Override
    public Object resolve(Injectee injectee) {
        System.out.println("RESOLVE "+injectee);
        if (injectee.getRequiredQualifiers().size() > 1) {
            throw new IllegalStateException("Multiple qualifiers not supported");
        }
        Annotation ann = injectee.getRequiredQualifiers().isEmpty() ? null
                : injectee.getRequiredQualifiers().iterator().next();

        Type type = injectee.getRequiredType();
        if (injectee.isFactory()) {
            // have to emulate factory case
            type = GenericsResolver.resolve(GenericsUtils.resolveClass(type, EmptyGenericsMap.getInstance()))
                    .type(Supplier.class)
                    .generic(0);
        }


//        if (injectee.getInjecteeDescriptor() != null) {
//            // todo may not be correct at all!!!
//            final Key key = (Key) injectee.getInjecteeDescriptor().get();
//            return injectee.isOptional() && injector.getExistingBinding(key) == null ? null
//                    : injector.getInstance(key);
//        }

        // todo select first bean because it could possibly be a contract
        Key key = ann == null ? Key.get(injectee.getRequiredType()) : Key.get(injectee.getRequiredType(), ann);
        Object res = injectee.isOptional() && injector.get().getExistingBinding(key) == null ? null
                : injector.get().getInstance(key);
        if (res != null) {
            if (injectee.isFactory()) {
                // again factory case emulation
                return (Supplier<Object>) () -> res;
            }

            // note that provider case will work automatically
        }

        return null;
    }

    @Override
    public boolean isConstructorParameterIndicator() {
        return true;
    }

    @Override
    public boolean isMethodParameterIndicator() {
        return false;
    }

    @Override
    public Class<Context> getAnnotation() {
        return Context.class;
    }

    /**
     * Context injection resolver binder.
     */
    public static final class Binder extends AbstractBinder {

        private Supplier<Injector> injector;

        public Binder(Supplier<Injector> injector) {
            this.injector = injector;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void configure() {
            GuiceContextInjectionResolver resolver = new GuiceContextInjectionResolver(injector);

            /*
             * Binding for CDI, without this binding JerseyInjectionTarget wouldn't know about the ContextInjectionTarget and
             * injection into fields would be disabled.
             */
            bind(resolver)
                    .to(new GenericType<InjectionResolver<Context>>() {})
                    .to(ContextInjectionResolver.class);

            /*
             * Binding for Jersey, without this binding Jersey wouldn't put together ContextInjectionResolver and
             * DelegatedInjectionValueParamProvider and therefore injection into resource method would be disabled.
             */
            bind(Bindings.service(resolver))
                    .to(new GenericType<InjectionResolver<Context>>() {})
                    .to(ContextInjectionResolver.class);
        }
    }
}
