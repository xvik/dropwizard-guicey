package ru.vyarus.dropwizard.guice.debug.report.guice.util;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.internal.util.StackTraceElements;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementSource;
import ru.vyarus.dropwizard.guice.debug.report.guice.model.BindingDeclaration;
import ru.vyarus.dropwizard.guice.debug.report.guice.model.ModuleDeclaration;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.visitor.GuiceScopingVisitor;
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;
import ru.vyarus.java.generics.resolver.util.map.EmptyGenericsMap;

import java.lang.annotation.Annotation;
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

    private static final GuiceScopingVisitor SCOPE_DETECTOR = new GuiceScopingVisitor();

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
            final Key key = it.getKey();
            // Duplicate could be in private modules: two bindings with the same key - one is real declaration and
            // another is expose declaration. The Original declaration could be a linked declaration, which
            // is more important than expose (considering that this index is used for jit and removed detection)
            if (key != null && (!res.containsKey(key) || it.getTarget() != null)) {
                res.put(key, it);
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
     * Detects binding scope.
     *
     * @param binding binding
     * @return binding scope
     */
    public static Class<? extends Annotation> getScope(final Binding<?> binding) {
        Class<? extends Annotation> scope = SCOPE_DETECTOR.performDetection(binding);
        if (scope != null && scope.equals(EagerSingleton.class)) {
            scope = javax.inject.Singleton.class;
        }
        return scope;
    }

    /**
     * @param key guice binding key
     * @return string representation for key or "-" if key is null
     */
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public static String renderKey(final Key key) {
        if (key == null) {
            return "-";
        }
        final StringBuilder res = new StringBuilder();
        if (key.getAnnotationType() != null) {
            res.append('@').append(key.getAnnotationType().getSimpleName());
            for (Method method : key.getAnnotationType().getMethods()) {
                if ("value".equals(method.getName()) && method.getReturnType().equals(String.class)) {
                    final boolean accessible = method.canAccess(key.getAnnotation());
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
            res.append(' ');
        }
        res.append(TypeToStringUtils.toStringType(key.getTypeLiteral().getType(), EmptyGenericsMap.getInstance()));
        return res.toString();
    }

    /**
     * NOTE: this will work only for elements, parsed with SPI api, and not for real bindings!
     *
     * @param element guice binding element
     * @return element declaration stacktrace element
     */
    public static StackTraceElement getDeclarationSource(final Element element) {
        final Object source = element.getSource();
        StackTraceElement traceElement = null;
        if (source instanceof ElementSource) {
            final ElementSource src = (ElementSource) source;
            if (src.getDeclaringSource() instanceof StackTraceElement) {
                traceElement = (StackTraceElement) src.getDeclaringSource();
            } else if (src.getDeclaringSource() instanceof Method) {
                traceElement = (StackTraceElement) StackTraceElements.forMember((Method) src.getDeclaringSource());
            }
        }
        return traceElement;
    }

    /**
     * Render element declaration source.
     *
     * @param element guice element
     * @return element declaration source or null
     */
    public static String renderSource(final Element element) {
        final StackTraceElement trace = getDeclarationSource(element);
        String res = null;
        if (trace != null) {
            // using full stacktrace element to grant proper link highlight in idea
            res = trace.toString();
        } else {
            final Object source = element.getSource();
            // source instanceof Class - JIT binding
            if (source instanceof String) {
                // possible for synthetic bindings, created by guicey for extensions not directly exposed in
                // private modules
                res = (String) source;
            }
        }
        return res;
    }
}
