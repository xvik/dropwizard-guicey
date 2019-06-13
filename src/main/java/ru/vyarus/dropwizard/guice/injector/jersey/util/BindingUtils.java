package ru.vyarus.dropwizard.guice.injector.jersey.util;

import com.google.inject.Key;
import org.glassfish.jersey.internal.inject.*;
import ru.vyarus.dropwizard.guice.injector.jersey.web.JerseyWeb;
import ru.vyarus.java.generics.resolver.GenericsResolver;
import ru.vyarus.java.generics.resolver.context.container.ParameterizedTypeImpl;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;
import ru.vyarus.java.generics.resolver.util.TypeUtils;
import ru.vyarus.java.generics.resolver.util.map.EmptyGenericsMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Vyacheslav Rusakov
 * @since 27.04.2019
 */
public class BindingUtils {

    public static String toStringKey(Class<?> contractOrImpl, Annotation[] qualifiers) {
        String res = Arrays.stream(qualifiers).map(it -> "@" + it.annotationType().getSimpleName())
                .collect(Collectors.joining(", "));
        if (!res.isEmpty()) {
            res += " ";
        }
        return res + contractOrImpl.getName();
    }

    public static String toStringBinding(Binding<?, ? extends Binding> binding) {
        final Class bindingType = binding.getClass();
        final StringBuilder res = new StringBuilder("[").append(bindingType.getSimpleName()).append("] ");

        if (binding.getName() != null) {
            res.append("name '").append(binding.getName()).append("' ");
        }

        if (binding.getRank() != null) {
            res.append(" rank").append(binding.getRank()).append(" ");
        }

        if (!binding.getQualifiers().isEmpty()) {
            for (Annotation q : binding.getQualifiers()) {
                res.append("@").append(q.annotationType().getSimpleName()).append(" ");
            }
        }

        if (ClassBinding.class.isAssignableFrom(bindingType)) {
            ClassBinding bind = (ClassBinding) binding;
            res.append(bind.getService().getName()).append(" ");

        } else if (InstanceBinding.class.isAssignableFrom(bindingType)) {
            InstanceBinding bind = (InstanceBinding) binding;
            res.append(bind.getService().getClass().getName()).append(" ");

        } else if (SupplierClassBinding.class.isAssignableFrom(bindingType)) {
            SupplierClassBinding<?> bind = (SupplierClassBinding) binding;

            Class supplier = bind.getSupplierClass();
            Class type = GenericsResolver.resolve(supplier).type(Supplier.class).generic(0);
            // type may be Object - in this case contracts will contain proper types

            res.append(supplier.getName()).append(" (")
                    .append(new ParameterizedTypeImpl(Supplier.class, type)).append(") ");

            if (bind.getSupplierScope() != null) {
                res.append("supplier scope @").append(bind.getSupplierScope().getSimpleName()).append(" ");
            }
        } else if (SupplierInstanceBinding.class.isAssignableFrom(bindingType)) {
            SupplierInstanceBinding<?> bind = (SupplierInstanceBinding) binding;
            Class supplier = bind.getSupplier().getClass();
            Class type = GenericsResolver.resolve(supplier).type(Supplier.class).generic(0);
            // type may be Object - in this case contracts will contain proper types

            res.append(supplier.getName()).append(" (")
                    .append(new ParameterizedTypeImpl(Supplier.class, type)).append(") ");

        } else if (InjectionResolverBinding.class.isAssignableFrom(bindingType)) {
            InjectionResolverBinding bind = (InjectionResolverBinding) binding;

            Class<Annotation> ann = bind.getResolver().getAnnotation();

            Class<? extends InjectionResolver> resolver = bind.getResolver().getClass();
            res.append(resolver.getName()).append(" (")
                    .append(new ParameterizedTypeImpl(InjectionResolver.class, ann)).append(") ");
        } else {
            res.append(" !!UNSUPPORTED BINDING!! ");
        }


        if (binding.getScope() != null) {
            res.append("scope @").append(binding.getScope().getSimpleName()).append(" ");
        }

        if (!binding.getContracts().isEmpty()) {
            res.append("contracts: [").append(binding.getContracts().stream()
                    .map(it -> TypeToStringUtils.toStringType(it, EmptyGenericsMap.getInstance()))
                    .collect(Collectors.joining(", "))).append("] ");
        }

        if (!binding.getAliases().isEmpty()) {
            res.append("aliases: [")
                    .append(binding.getAliases().stream().map(alias -> {
                        StringBuilder result = new StringBuilder();
                        if (alias.getRank().isPresent()) {
                            result.append("rank ").append(alias.getRank().getAsInt()).append(" ");
                        }
                        if (!alias.getQualifiers().isEmpty()) {
                            result.append(alias.getQualifiers().stream().map(it -> "@" + it.annotationType().getSimpleName())
                                    .collect(Collectors.joining())).append(" ");
                        }

                        result.append(alias.getContract().getName()).append(" ");
                        if (alias.getScope().isPresent()) {
                            result.append("scope @").append(alias.getScope().get());
                        }
                        return result.toString();
                    }).collect(Collectors.joining(", "))).append("] ");
        }
        return res.toString();
    }

    public static Class<? extends Annotation> extractQualifier(Collection<Annotation> qualifiers) {
        // important to get type, because jersey often provides CustomAnnotationLiteral instead of @Custom
        return qualifiers.isEmpty() ? null : qualifiers.iterator().next().annotationType();
    }

    public static Key buildKey(Binding<?, ?> binding) {
        Class<? extends Annotation> ann = extractQualifier(binding.getQualifiers());

        Key key = null;

        if (ClassBinding.class.isAssignableFrom(binding.getClass())) {
            ClassBinding bind = (ClassBinding) binding;
            key = ann == null ? com.google.inject.Key.get(bind.getImplementationType())
                    : com.google.inject.Key.get(bind.getImplementationType(), ann);

        } else if (InstanceBinding.class.isAssignableFrom(binding.getClass())) {
            InstanceBinding bind = (InstanceBinding) binding;
            key = ann == null ? com.google.inject.Key.get(bind.getImplementationType())
                    : com.google.inject.Key.get(bind.getImplementationType(), ann);

        } else if (SupplierClassBinding.class.isAssignableFrom(binding.getClass())) {
            SupplierClassBinding<?> bind = (SupplierClassBinding) binding;

            Class supplier = bind.getSupplierClass();
            Type type = GenericsResolver.resolve(supplier).type(Supplier.class).genericType(0);
            // type may be Object, but in this case contracts will be present
            // for sure all types must be compatible so just need to select the most specific type
            type = findSupplierType(type, bind.getContracts());

            // special binding for web bindings, colliding with guice ServletModule
            if (collideServletModule(type)) {
                key = com.google.inject.Key.get(type, JerseyWeb.class);
            } else {
                key = ann == null ? com.google.inject.Key.get(type)
                        : com.google.inject.Key.get(type, ann);
            }

        } else if (SupplierInstanceBinding.class.isAssignableFrom(binding.getClass())) {
            SupplierInstanceBinding<?> bind = (SupplierInstanceBinding) binding;

            Class supplier = bind.getSupplier().getClass();
            Type type = GenericsResolver.resolve(supplier).type(Supplier.class).genericType(0);
            // type may be Object, but in this case contracts will be present
            // for sure all types must be compatible so just need to select the most specific type
            type = findSupplierType(type, bind.getContracts());

            // special binding for web bindings, colliding with guice ServletModule
            if (collideServletModule(type)) {
                key = com.google.inject.Key.get(type, JerseyWeb.class);
            } else {
                key = ann == null ? com.google.inject.Key.get(type)
                        : com.google.inject.Key.get(type, ann);
            }

        } else if (InjectionResolverBinding.class.isAssignableFrom(binding.getClass())) {
            InjectionResolverBinding bind = (InjectionResolverBinding) binding;

            Type type = new ParameterizedTypeImpl(InjectionResolver.class, bind.getResolver().getAnnotation());

            key = ann == null ? com.google.inject.Key.get(type)
                    : com.google.inject.Key.get(type, ann);
        }
        return key;
    }

    public static void filterNotQualifiedKeys(List<Key> keys, Class<? extends Annotation> qualifier) {
        if (qualifier != null) {
            keys.removeIf(next -> next.getAnnotation() != null
                    && !qualifier.equals(next.getAnnotation().annotationType()));
        }
    }


    /**
     * There are possibly multiple types, leading to the same supplier (e.g. ContainerRequestContext, ContainerRequest),
     * but these types must be compatible, so we have to find the most specific type and use it as key.
     *
     * @param type      type resolved with reflection from supplier implementation class
     * @param contracts contracts declared for supplier
     * @return the most generic type
     */
    private static Type findSupplierType(Type type, Set<Type> contracts) {
        Type cand = type == Object.class ? null : type;
        for (Type ctr : contracts) {
            if (cand == null || TypeUtils.isMoreSpecific(ctr, cand)) {
                cand = ctr;
            }
        }
        return cand;
    }

    private static boolean collideServletModule(Type type) {
        return type.equals(ServletContext.class)
                || type.equals(HttpServletRequest.class)
                || type.equals(HttpServletResponse.class);
    }

    public static String toStringKey(Key key) {
        String res = "";
        if (key.getAnnotation() != null) {
            res += "@" + key.getAnnotationType().getName();
        }
        final Type type = key.getTypeLiteral().getType();
        res += TypeToStringUtils.toStringType(type);
        return res;
    }
}
