package ru.vyarus.dropwizard.guice.module.installer.util;

import com.google.inject.Binding;
import com.google.inject.Module;
import com.google.inject.ScopeAnnotation;
import com.google.inject.internal.util.StackTraceElements;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementSource;
import javax.inject.Scope;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Guice bindings utils.
 *
 * @author Vyacheslav Rusakov
 * @since 29.08.2019
 */
public final class BindingUtils {

    /**
     * Used to mark undeclared elemnts (not declared manually in any module).
     */
    public static final String JIT_MODULE = "JIT";

    private BindingUtils() {
    }

    /**
     * Resolve guice configuration element declaration module chain. The first list element is declaration module.
     * Later elements appear if module was installed by other module.
     * <p>
     * Modules declared with lambda expression removed. If there are upper modules, declared normally then
     * only they would remain (in report bindings would be should directly as module's bindings). When no upper
     * modules exists then returned chain would contain only {@link com.google.inject.Module} to group all bindings
     * from all root lambda modules into single "meta" module in the report (code links would still be correct in
     * the report).
     *
     * @param element guice SPI element
     * @return modules chain from declaration point or single {@link #JIT_MODULE} if binding is a JIT binding
     */
    public static List<String> getModules(final Element element) {
        final List<String> modules;
        if (element.getSource() instanceof ElementSource) {
            ElementSource source = (ElementSource) element.getSource();
            // if module was repackaged by elements api, we need an original source in order to build correct report
            if (source.getOriginalElementSource() != null) {
                source = source.getOriginalElementSource();
            }
            modules = replaceLambdaModuleClasses(source.getModuleClassNames());
        } else {
            // consider JIT binding
            modules = Collections.singletonList(JIT_MODULE);
        }
        return modules;
    }

    /**
     * Resolve class from module name, resolved from guice {@link Element} source. May return {@link Module} if
     * element was a jit binding and provided name is {@link #JIT_MODULE}.
     *
     * @param name module full class name (or jit marker)
     * @return module class or {@link Module} for jit marker
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Module> getModuleClass(final String name) {
        final Class<? extends Module> res;
        if (!JIT_MODULE.equals(name)) {
            try {
                res = (Class) Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Failed to resolve module class", e);
            }
        } else {
            // for JIT bindings use pure interface as module name
            res = Module.class;
        }
        return res;
    }

    /**
     * May return {@link Module} if JIT binding provided.
     *
     * @param element guice SPI element
     * @return top module class name
     */
    public static Class<? extends Module> getTopDeclarationModule(final Element element) {
        final List<String> modulesStack = getModules(element);
        // top most element is top most module (registered by user)
        return getModuleClass(modulesStack.get(modulesStack.size() - 1));
    }

    /**
     * Resolve binding declaration source string, if possible.
     *
     * @param binding binding
     * @return binding declaration source
     */
    public static String getDeclarationSource(final Binding binding) {
        String res = "UNKNOWN";
        final Object source = binding.getSource();
        if (source instanceof ElementSource) {
            final ElementSource src = (ElementSource) source;
            StackTraceElement traceElement = null;
            if (src.getDeclaringSource() instanceof StackTraceElement) {
                traceElement = (StackTraceElement) src.getDeclaringSource();
            } else if (src.getDeclaringSource() instanceof Method) {
                traceElement = (StackTraceElement) StackTraceElements.forMember((Method) src.getDeclaringSource());
            }
            if (traceElement != null) {
                res = traceElement.toString();
            }
        } else if (source instanceof Class) {
            res = ((Class) source).getName();
        }
        return res;
    }

    private static List<String> replaceLambdaModuleClasses(final List<String> modules) {
        List<String> detected = null;
        for (String module : modules) {
            // most likely, lambda expression
            if (module.contains("$$")) {
                try {
                    Class.forName(module);
                } catch (ClassNotFoundException ex) {
                    if (detected == null) {
                        detected = new ArrayList<>();
                    }
                    detected.add(module);
                }
            }
        }
        final List<String> res = detected == null ? modules : new ArrayList<>(modules);
        if (detected != null) {
            // remove all lambda references
            res.removeAll(detected);
            // If there is a chain of modules then DO NOT ADD default module to let these bindings
            // be directly shown under custom module (installing lambda module) in the report.
            // If no upper module, then adding default module to gather all lambda module's bindings
            // under one node in report (Module).
            // NOTE: ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo.getModules would
            // still return root lambda modules (only root!) because all root module classes are stored
            // on registration, but there would not be any possibility to disable such module
            if (res.isEmpty()) {
                res.add(Module.class.getName());
            }
        }
        return res;
    }

    /**
     * Searches for scoping annotation on class. Base classes are not checked as scope is not inheritable.
     *
     * @param type               class to search for
     * @param countGuiceSpecific true to count guice-specific annotations (with {@link ScopeAnnotation})
     * @return detected annotation or null
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Annotation> findScopingAnnotation(final Class<?> type,
                                                                    final boolean countGuiceSpecific) {
        Class<? extends Annotation> res = null;
        Class<? extends Annotation> jakartaScope = null;
        try {
            jakartaScope = (Class<? extends Annotation>) Class.forName("jakarta.inject.Scope");
        } catch (Exception ignored) {
            // no jakarta annotations in classpath
        }

        for (Annotation ann : type.getAnnotations()) {
            final Class<? extends Annotation> annType = ann.annotationType();
            if (annType.isAnnotationPresent(Scope.class)
                    || (jakartaScope != null && annType.isAnnotationPresent(jakartaScope))) {
                res = annType;
                break;
            }
            // guice has special marker annotation
            if (countGuiceSpecific && annType.isAnnotationPresent(ScopeAnnotation.class)) {
                res = annType;
                break;
            }
        }
        return res;
    }
}
