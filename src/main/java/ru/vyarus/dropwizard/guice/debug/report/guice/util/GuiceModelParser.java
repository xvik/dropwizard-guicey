package ru.vyarus.dropwizard.guice.debug.report.guice.util;

import com.google.common.base.Preconditions;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.internal.util.StackTraceElements;
import com.google.inject.servlet.ServletModule;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementSource;
import com.google.inject.spi.PrivateElements;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.debug.report.guice.model.BindingDeclaration;
import ru.vyarus.dropwizard.guice.debug.report.guice.model.ModuleDeclaration;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.visitor.GuiceElementVisitor;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.visitor.GuiceScopingVisitor;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.visitor.PrivateModuleException;
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Guice introspection utils. Parse guice SPI model. Supports both elements from modules and injector bindings.
 *
 * @author Vyacheslav Rusakov
 * @since 14.08.2019
 */
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public final class GuiceModelParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuiceModelParser.class);
    private static final GuiceScopingVisitor SCOPE_DETECTOR = new GuiceScopingVisitor();
    private static final GuiceElementVisitor ELEMENT_VISITOR = new GuiceElementVisitor();
    private static final String JIT_MODULE = "JIT";

    private GuiceModelParser() {
    }

    /**
     * Parse guice elements into modules tree. Only direct bindings are accepted. Supports both module elements
     * and injector bindings (but result will slightly vary). JIT bindings are also grouped into special module.
     *
     * @param injector injector instance
     * @param elements elements to analyze
     * @return tree of modules.
     */
    public static List<ModuleDeclaration> parse(final Injector injector,
                                                final Collection<? extends Element> elements) {
        final Map<String, ModuleDeclaration> index = indexElements(injector, elements);
        final List<ModuleDeclaration> res = new ArrayList<>();

        // build modules tree
        for (ModuleDeclaration mod : index.values()) {
            if (mod.getParent() != null) {
                index.get(mod.getParent()).getChildren().add(mod);
            } else {
                res.add(mod);
            }
            // sort declarations according to declaration order (in module)
            mod.getDeclarations().sort(Comparator
                    .comparingInt(BindingDeclaration::getSourceLine)
                    .thenComparing(BindingDeclaration::getType)
                    .thenComparing(it -> it.getScope() != null ? it.getScope().getSimpleName() : "")
                    .thenComparing(it -> it.getKey() != null ? GuiceModelUtils.renderKey(it.getKey()) : ""));
        }

        // sort entire tree by module name for predictable order
        final Comparator<ModuleDeclaration> moduleComparator = Comparator.comparing(it -> it.getType().getName());
        res.sort(moduleComparator);
        GuiceModelUtils.visit(res, it -> it.getChildren().sort(moduleComparator));

        // return only root modules
        return res;
    }

    /**
     * Parse single guice element.
     *
     * @param injector injector instance
     * @param element  element to analyze
     * @return parsed descriptor or null if element is not supported (or intentionally skipped)
     */
    public static BindingDeclaration parseElement(final Injector injector, final Element element) {
        final BindingDeclaration dec = element.acceptVisitor(ELEMENT_VISITOR);

        if (dec != null) {
            fillDeclaration(dec, injector);
            fillSource(dec, element.getSource());
            dec.setModule(getModules(element).get(0));

            if (dec.getKey() != null) {
                final Class ann = dec.getKey().getAnnotationType();
                if (ann != null) {
                    if (ann.getName().equals("com.google.inject.internal.Element")) {
                        dec.setSpecial(Collections.singletonList("multibinding"));
                    }
                    if (ann.getName().startsWith("com.google.inject.internal.RealOptionalBinder")) {
                        dec.setSpecial(Collections.singletonList("optional binding"));
                    }
                }
            }
        }

        return dec;
    }

    private static Map<String, ModuleDeclaration> indexElements(final Injector injector,
                                                                final Collection<? extends Element> elements) {
        final Map<String, ModuleDeclaration> index = new LinkedHashMap<>();
        for (Element element : elements) {
            try {
                final BindingDeclaration dec = parseElement(injector, element);
                if (dec == null) {
                    continue;
                }
                // create modules for entire modules chains
                final ModuleDeclaration mod = initModules(index, getModules(element));
                mod.getDeclarations().add(dec);
            } catch (PrivateModuleException ex) {
                // private module appeared
                indexPrivate(index, injector, ex.getElements());
            }
        }
        return index;
    }

    private static void indexPrivate(final Map<String, ModuleDeclaration> index, final Injector injector,
                                     final PrivateElements elements) {
        // indicate module where private module was registered
        final String declaringModule = ((ElementSource) elements.getSource()).getModuleClassNames().get(0);
        // private modules structure
        final Map<String, ModuleDeclaration> privateIndex =
                indexElements(injector, elements.getElements());
        final Set<Key<?>> exposed = elements.getExposedKeys();

        for (Map.Entry<String, ModuleDeclaration> entry : privateIndex.entrySet()) {
            final String key = entry.getKey();
            // skip already known above hierarchy
            if (index.containsKey(key)) {
                continue;
            }
            final ModuleDeclaration mod = entry.getValue();
            if (declaringModule.equals(key)) {
                // mark as private ONLY private module registration point
                mod.setPrivateModule(true);
                mod.getMarkers().add("PRIVATE");
            }

            // indicate exposed bindings (even though explicit expose items will be shown additionally)
            GuiceModelUtils.visitBindings(Collections.singletonList(mod), it -> {
                if (it.getKey() != null && exposed.contains(it.getKey())) {
                    it.getMarkers().add("EXPOSED");
                }
            });

            // add module to main index
            index.put(key, mod);
        }

        // exposed bindings are not available for modules analysis.. manually adding them
        for (Key<?> key : exposed) {
            final Binding<?> existingBinding = injector.getExistingBinding(key);
            // may be null if multiple private module levels appear - only first level exposure shown
            if (existingBinding != null) {
                final BindingDeclaration dec = parseElement(injector, existingBinding);
                index.get(dec.getModule()).getDeclarations().add(dec);
            }
        }
    }

    private static List<String> getModules(final Element element) {
        final List<String> modules;
        if (element.getSource() instanceof ElementSource) {
            modules = ((ElementSource) element.getSource()).getModuleClassNames();
        } else {
            // consider JIT binding
            modules = Collections.singletonList(JIT_MODULE);
        }
        return modules;
    }

    @SuppressFBWarnings("NP_NULL_PARAM_DEREF")
    private static ModuleDeclaration initModules(final Map<String, ModuleDeclaration> index,
                                                 final List<String> path) {
        ModuleDeclaration res = null;
        // important to check entire path because of possible modules without bindings (installing other modules only)
        String name;
        String parent;
        for (int i = 0; i < path.size(); i++) {
            name = path.get(i);
            parent = i < path.size() - 1 ? path.get(i + 1) : null;

            final ModuleDeclaration mod = initModule(index, name);
            mod.setParent(parent);
            Preconditions.checkState(mod.getParent() == null || Objects.equals(mod.getParent(), parent),
                    "Parents don't match for module %s: '%s' and '%s' in path (%s)",
                    name, mod.getParent(), parent, String.join("-", path));
            if (res == null) {
                res = mod;
            }
        }
        return res;
    }

    private static ModuleDeclaration initModule(final Map<String, ModuleDeclaration> index,
                                                final String name) {
        ModuleDeclaration mod = index.get(name);
        if (mod == null) {
            mod = new ModuleDeclaration();
            if (!name.equals(JIT_MODULE)) {
                try {
                    mod.setType(Class.forName(name));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Failed to resolve module class", e);
                }
            } else {
                // for JIT bindings use pure interface as module name
                mod.setType(Module.class);
            }
            if (ServletModule.class.isAssignableFrom(mod.getType())) {
                mod.getMarkers().add("WEB");
            }
            index.put(name, mod);
        }
        return mod;
    }

    @SuppressWarnings("unchecked")
    private static void fillDeclaration(final BindingDeclaration dec, final Injector injector) {
        if (dec.getKey() != null) {
            // use binding from injector to get actually configured scope (for cases when element come from modules
            // analysis)
            Binding existingBinding = injector.getExistingBinding(dec.getKey());
            if (existingBinding == null) {
                // could happen with servlet modules
                if (dec.getElement() instanceof Binding) {
                    existingBinding = (Binding) dec.getElement();
                } else {
                    return;
                }
            }
            Class<? extends Annotation> scope =
                    (Class<? extends Annotation>) existingBinding.acceptScopingVisitor(SCOPE_DETECTOR);
            if (scope != null && scope.equals(EagerSingleton.class)) {
                scope = javax.inject.Singleton.class;
            }
            dec.setScope(scope);
            // important for untargetted bindings to look existing binding
            if (existingBinding instanceof ConstructorBinding) {
                final int aops = ((ConstructorBinding) existingBinding).getMethodInterceptors().size();
                if (aops > 0) {
                    dec.getMarkers().add("AOP");
                }
            }
        }
    }

    private static void fillSource(final BindingDeclaration dec, final Object source) {
        if (source instanceof ElementSource) {
            final ElementSource src = (ElementSource) source;
            StackTraceElement traceElement = null;
            if (src.getDeclaringSource() instanceof StackTraceElement) {
                traceElement = (StackTraceElement) src.getDeclaringSource();
            } else if (src.getDeclaringSource() instanceof Method) {
                traceElement = (StackTraceElement) StackTraceElements.forMember((Method) src.getDeclaringSource());
            }
            if (traceElement != null) {
                // using full stacktrace element to grant proper link highlight in idea
                dec.setSource(traceElement.toString());
                dec.setSourceLine(traceElement.getLineNumber());
            }
        } else if (source instanceof Class) {
            dec.setSource(((Class) source).getName());
        } else {
            LOGGER.warn("Unknown element '{}' source: {}", dec, source);
        }
    }

}
