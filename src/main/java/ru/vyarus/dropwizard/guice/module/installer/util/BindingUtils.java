package ru.vyarus.dropwizard.guice.module.installer.util;

import com.google.inject.Binding;
import com.google.inject.Module;
import com.google.inject.internal.util.StackTraceElements;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementSource;

import java.lang.reflect.Method;
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
            modules = source.getModuleClassNames();
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
        if (!name.equals(JIT_MODULE)) {
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
}
