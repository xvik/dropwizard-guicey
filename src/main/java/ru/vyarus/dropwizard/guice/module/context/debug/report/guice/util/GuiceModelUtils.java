package ru.vyarus.dropwizard.guice.module.context.debug.report.guice.util;

import com.google.inject.Key;
import com.google.inject.Module;
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.model.BindingDeclaration;
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.model.ModuleDeclaration;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;
import ru.vyarus.java.generics.resolver.util.map.EmptyGenericsMap;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * Utilities for parsed guice model analysis.
 *
 * @author Vyacheslav Rusakov
 * @since 15.08.2019
 */
public final class GuiceModelUtils {

    private GuiceModelUtils() {
    }

    /**
     * @param modules modules tree
     * @return list of module classes, used in tree or empty list
     */
    public static List<Class> getModules(final List<ModuleDeclaration> modules) {
        if (modules.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Class> res = new ArrayList<>();
        visit(modules, it -> res.add(it.getType()));
        // jit bindings are grouped with special module
        res.remove(Module.class);
        return res;
    }

    /**
     * @param modules modules tree
     * @return index of bindings with non null key or empty map
     */
    public static Map<Key, BindingDeclaration> index(final List<ModuleDeclaration> modules) {
        if (modules.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<Key, BindingDeclaration> res = new HashMap<>();
        visitBindings(modules, it -> {
            if (it.getKey() != null) {
                res.put(it.getKey(), it);
            }
        });
        return res;
    }

    /**
     * Applies callback to all modules in tree.
     *
     * @param modules  modules tree
     * @param consumer callback
     */
    public static void visit(final List<ModuleDeclaration> modules,
                             final Consumer<ModuleDeclaration> consumer) {
        for (ModuleDeclaration mod : modules) {
            consumer.accept(mod);
            visit(mod.getChildren(), consumer);
        }
    }

    /**
     * Applies callback to all bindings in tree.
     *
     * @param modules  modules tree
     * @param consumer callback
     */
    public static void visitBindings(final List<ModuleDeclaration> modules,
                                     final Consumer<BindingDeclaration> consumer) {
        visit(modules, it -> it.getDeclarations().forEach(consumer));
    }

    /**
     * @param key guice binding key
     * @return string representation for key or "-" if key is null
     */
    public static String renderKey(final Key key) {
        if (key == null) {
            return "-";
        }
        final StringBuilder res = new StringBuilder();
        if (key.getAnnotationType() != null) {
            res.append("@").append(key.getAnnotationType().getSimpleName());
            for (Method method : key.getAnnotationType().getMethods()) {
                if (method.getName().equals("value") && method.getReturnType().equals(String.class)) {
                    final boolean accessible = method.isAccessible();
                    try {
                        method.setAccessible(true);
                        final String qualifier = (String) method.invoke(key.getAnnotation());
                        if (qualifier != null && !qualifier.isEmpty()) {
                            res.append("(\"").append(qualifier).append("\")");
                        }
                        break;
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to inspect annotation", e);
                    } finally {
                        method.setAccessible(accessible);
                    }
                }
            }
            res.append(" ");
        }
        res.append(TypeToStringUtils.toStringType(key.getTypeLiteral().getType(), EmptyGenericsMap.getInstance()));
        return res.toString();
    }
}
