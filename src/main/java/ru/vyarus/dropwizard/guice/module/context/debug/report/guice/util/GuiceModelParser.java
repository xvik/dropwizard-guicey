package ru.vyarus.dropwizard.guice.module.context.debug.report.guice.util;

import com.google.common.base.Preconditions;
import com.google.inject.*;
import com.google.inject.internal.util.StackTraceElements;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;
import com.google.inject.servlet.SessionScoped;
import com.google.inject.spi.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.model.BindingDeclaration;
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.model.DeclarationType;
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.model.ModuleDeclaration;
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton;
import ru.vyarus.dropwizard.guice.module.support.scope.Prototype;

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
    private static final ClassDefaultBindingScopingVisitor SCOPE_DETECTOR = new ClassDefaultBindingScopingVisitor();
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
                    .thenComparing(BindingDeclaration::getType));
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
        final DeclarationType type = DeclarationType.detect(element.getClass());
        if (type == null) {
            LOGGER.debug("Unknown element type: {}", element);
            return null;
        }
        // provider binding is synthetic binding when do bind().toProvider() (right part)
        // we need to show only actual bindings so skipping it
        return type == DeclarationType.ProviderBinding ? null : buildDeclaration(type, element, injector);
    }

    private static Map<String, ModuleDeclaration> indexElements(final Injector injector,
                                                                final Collection<? extends Element> elements) {
        final Map<String, ModuleDeclaration> index = new LinkedHashMap<>();
        for (Element element : elements) {
            final List<String> modules;
            if (element.getSource() instanceof ElementSource) {
                modules = ((ElementSource) element.getSource()).getModuleClassNames();
            } else {
                // consider JIT binding
                modules = Collections.singletonList(JIT_MODULE);
            }

            final BindingDeclaration dec = parseElement(injector, element);
            if (dec == null) {
                continue;
            }
            // create modules for entire modules chains
            final ModuleDeclaration mod = initModules(index, modules);
            mod.getDeclarations().add(dec);
        }
        return index;
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

    @SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:ExecutableStatementCount",
            "checkstyle:JavaNCSS"})
    private static BindingDeclaration buildDeclaration(final DeclarationType type,
                                                       final Element element,
                                                       final Injector injector) {
        final BindingDeclaration dec = new BindingDeclaration(type);
        Key key = null;

        switch (dec.getType()) {
            case Scope:
                final ScopeBinding scopeBinding = (ScopeBinding) element;
                dec.setScope(scopeBinding.getAnnotationType());
                break;
            case Instance:
                final InstanceBinding instanceBinding = (InstanceBinding) element;
                key = instanceBinding.getKey();
                break;
            case InstanceProvider:
                final ProviderInstanceBinding providerInstanceBinding = (ProviderInstanceBinding) element;
                key = providerInstanceBinding.getKey();
                dec.setProvidedBy(providerInstanceBinding.getUserSuppliedProvider().toString());
                break;
            case LinkedKey:
                final LinkedKeyBinding keyBinding = (LinkedKeyBinding) element;
                key = keyBinding.getKey();
                dec.setTarget(keyBinding.getLinkedKey());
                break;
            case KeyProvider:
                final ProviderKeyBinding providerKeyBinding = (ProviderKeyBinding) element;
                key = providerKeyBinding.getKey();
                dec.setProvidedBy(GuiceModelUtils.renderKey(providerKeyBinding.getProviderKey()));
                break;
            case Untargetted:
                final UntargettedBinding untargettedBinding = (UntargettedBinding) element;
                key = untargettedBinding.getKey();
                break;
            case Aop:
                final InterceptorBinding interceptorBinding = (InterceptorBinding) element;
                dec.setSpecial(interceptorBinding.getInterceptors());
                break;
            case TypeListener:
                final TypeListenerBinding typeListenerBinding = (TypeListenerBinding) element;
                dec.setSpecial(Collections.singletonList(typeListenerBinding.getListener()));
                break;
            case ProvisionListener:
                final ProvisionListenerBinding provisionListenerBinding = (ProvisionListenerBinding) element;
                dec.setSpecial(provisionListenerBinding.getListeners());
                break;
            case ProviderBinding:
                final ProviderBinding providerBinding = (ProviderBinding) element;
                key = providerBinding.getProvidedKey();
                break;
            case Binding:
                // case appear only with analyzing bindings from injector (in contrast to direct module elements
                // analysis). Here bindings are not differentiated by untargetted/key etc .. it's just binding.
                // And so the reports from Elements.getElements(modules) and injector.getAllBindings() would be
                // different (in bindings types only)
                final Binding binding = (Binding) element;
                key = binding.getKey();
                break;
            default:
                throw new IllegalStateException("Unsupported type: " + dec.getType());
        }
        dec.setKey(key);
        fillDeclaration(dec, injector);
        fillSource(dec, element);
        return dec;
    }

    @SuppressWarnings("unchecked")
    private static void fillDeclaration(final BindingDeclaration dec, final Injector injector) {
        if (dec.getKey() != null) {
            // use binding from injector to get actually configured scope (for cases when element come from modules
            // analysis)
            final Binding existingBinding = injector.getExistingBinding(dec.getKey());
            Class<? extends Annotation> scope =
                    (Class<? extends Annotation>) existingBinding.acceptScopingVisitor(SCOPE_DETECTOR);
            if (scope != null && scope.equals(EagerSingleton.class)) {
                scope = javax.inject.Singleton.class;
            }
            dec.setScope(scope);
            if (existingBinding instanceof ConstructorBinding) {
                final int aops = ((ConstructorBinding) existingBinding).getMethodInterceptors().size();
                if (aops > 0) {
                    dec.getMarkers().add("AOP");
                }
            }
        }
    }

    private static void fillSource(final BindingDeclaration dec, final Element element) {
        if (element.getSource() instanceof ElementSource) {
            final ElementSource source = (ElementSource) element.getSource();
            StackTraceElement traceElement = null;
            if (source.getDeclaringSource() instanceof StackTraceElement) {
                traceElement = (StackTraceElement) source.getDeclaringSource();
            } else if (source.getDeclaringSource() instanceof Method) {
                traceElement = (StackTraceElement) StackTraceElements.forMember((Method) source.getDeclaringSource());
            }
            if (traceElement != null) {
                // using full stacktrace element to grant proper link highlight in idea
                dec.setSource(traceElement.toString());
                dec.setSourceLine(traceElement.getLineNumber());
            }
        } else if (element.getSource() instanceof Class) {
            dec.setSource(((Class) element.getSource()).getName());
        } else {
            LOGGER.warn("Unknown element source: {}", element);
        }
    }

    /**
     * Guice binding scope analyzer. Does not support custom scopes.
     */
    private static class ClassDefaultBindingScopingVisitor
            extends DefaultBindingScopingVisitor<Class<? extends Annotation>> {

        @Override
        public Class<? extends Annotation> visitEagerSingleton() {
            return EagerSingleton.class;
        }

        @Override
        public Class<? extends Annotation> visitScope(final Scope scope) {
            Class<? extends Annotation> res = null;
            if (scope == Scopes.SINGLETON) {
                res = javax.inject.Singleton.class;
            }
            if (scope == Scopes.NO_SCOPE) {
                res = Prototype.class;
            }
            if (scope == ServletScopes.REQUEST) {
                res = RequestScoped.class;
            }
            if (scope == ServletScopes.SESSION) {
                res = SessionScoped.class;
            }
            // not supporting custom scopes
            return res;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends Annotation> visitScopeAnnotation(final Class scopeAnnotation) {
            return scopeAnnotation;
        }

        @Override
        public Class<? extends Annotation> visitNoScoping() {
            return Prototype.class;
        }
    }
}
