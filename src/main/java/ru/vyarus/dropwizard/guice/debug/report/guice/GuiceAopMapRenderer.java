package ru.vyarus.dropwizard.guice.debug.report.guice;

import com.google.inject.*;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.InterceptorBinding;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.aopalliance.intercept.MethodInterceptor;
import ru.vyarus.dropwizard.guice.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.debug.report.guice.model.ModuleDeclaration;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.GuiceModelParser;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.GuiceModelUtils;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.debug.util.TreeNode;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;
import ru.vyarus.java.generics.resolver.util.map.IgnoreGenericsMap;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Render guice AOP appliance map.
 *
 * @author Vyacheslav Rusakov
 * @since 23.08.2019
 */
@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
public class GuiceAopMapRenderer implements ReportRenderer<GuiceAopConfig> {

    private final Injector injector;
    private final List<Module> modules;

    public GuiceAopMapRenderer(final Injector injector) {
        this.injector = injector;
        final GuiceyConfigurationInfo info = injector.getInstance(GuiceyConfigurationInfo.class);
        // all modules (in case if overriding module declares aop)
        this.modules = info.getModules().stream()
                .map(it -> (Module) ((ModuleItemInfo) info.getInfo(it)).getInstance())
                .collect(Collectors.toList());
    }

    @Override
    public String renderReport(final GuiceAopConfig config) {
        final StringBuilder res = new StringBuilder();

        // AOP declarations
        final List<Element> declared = Elements.getElements(Stage.TOOL, modules).stream()
                .filter(it -> it instanceof InterceptorBinding)
                .collect(Collectors.toList());

        if (!config.isHideDeclarationsBlock()) {
            final List<ModuleDeclaration> declarationModules = GuiceModelParser.parse(injector, declared);
            res.append(Reporter.NEWLINE).append(Reporter.NEWLINE);
            renderDeclared(declarationModules, res);
        }

        if (!declared.isEmpty()) {
            // AOP appliance map
            final List<Binding> guiceManagedObjects = injector.getAllBindings().values().stream()
                    .filter(it -> it instanceof ConstructorBinding)
                    .collect(Collectors.toList());
            final List<AopDeclaration> tree = filter(GuiceModelParser.parse(injector, guiceManagedObjects), config);
            if (!tree.isEmpty()) {
                res.append(Reporter.NEWLINE).append(Reporter.NEWLINE);
                renderMap(tree, res);
            }
        }
        return res.toString();
    }

    private void renderDeclared(final List<ModuleDeclaration> declarations, final StringBuilder res) {
        final List<String> lines = new ArrayList<>();
        GuiceModelUtils.visitBindings(declarations, it -> {
            lines.add(String.format("%-70s    at %s",
                    it.getModule().substring(it.getModule().lastIndexOf('.') + 1) + "/"
                            + RenderUtils.getClassName(it.getSpecial().get(0).getClass()),
                    it.getSource()));
        });
        lines.sort(Comparator.naturalOrder());
        final TreeNode root = new TreeNode("%s AOP handlers declared", lines.size());
        for (String line : lines) {
            root.child(line);
        }
        root.render(res);
    }

    private void renderMap(final List<AopDeclaration> declarations,
                           final StringBuilder res) {
        if (!declarations.isEmpty()) {
            final TreeNode root = new TreeNode("%s bindings affected by AOP", declarations.size());
            for (AopDeclaration dec : declarations) {
                final TreeNode decl = root.child(GuiceModelUtils.renderKey(dec.getKey()) + "    ("
                        + RenderUtils.renderPackage(dec.getKey().getTypeLiteral().getRawType()) + ")");

                for (Map.Entry<Method, List<Class<? extends MethodInterceptor>>> entry
                        : dec.getInterceptors().entrySet()) {
                    final Method method = entry.getKey();
                    final List<Class<? extends MethodInterceptor>> handlers = entry.getValue();
                    String warn = "";
                    if (method.isSynthetic()) {
                        warn = "[SYNTHETIC] ";
                    }
                    String methodName = TypeToStringUtils.toStringMethod(method, IgnoreGenericsMap.getInstance());
                    // cut off return type
                    methodName = warn + methodName.substring(methodName.indexOf(method.getName()));
                    decl.child("%-60s      %s",
                            methodName,
                            handlers.stream().map(RenderUtils::getClassName).collect(Collectors.joining(", ")));
                }
            }
            root.render(res);
        }
    }

    @SuppressWarnings("unchecked")
    private List<AopDeclaration> filter(final List<ModuleDeclaration> modules, final GuiceAopConfig config) {
        final List<AopDeclaration> res = new ArrayList<>();
        GuiceModelUtils.visitBindings(modules, it -> {
            final Class<?> type = it.getKey().getTypeLiteral().getRawType();
            // filter by class
            if (config.getTypeMatcher() != null && !config.getTypeMatcher().matches(type)) {
                return;
            }
            final AopDeclaration dec = new AopDeclaration(it.getKey());
            final Map<Method, List<MethodInterceptor>> interceptors =
                    ((ConstructorBinding) it.getElement()).getMethodInterceptors();
            for (Map.Entry<Method, List<MethodInterceptor>> entry : interceptors.entrySet()) {
                final Method method = entry.getKey();
                // filter by method
                if (config.getMethodMatcher() != null && !config.getMethodMatcher().matches(method)) {
                    continue;
                }
                final List<Class<? extends MethodInterceptor>> handlers = entry.getValue()
                        .stream().map(MethodInterceptor::getClass).collect(Collectors.toList());
                // filter by required interceptors
                if (config.getShowOnly().isEmpty() || !Collections.disjoint(config.getShowOnly(), handlers)) {
                    dec.getInterceptors().put(method, handlers);
                }
            }
            if (!dec.getInterceptors().isEmpty()) {
                res.add(dec);
            }
        });
        // for predictable order in report
        res.sort(Comparator.comparing(it -> it.getKey().getTypeLiteral().getRawType().getSimpleName()));
        return res;
    }

    /**
     * Binding aop map declaration (filtered).
     */
    private static class AopDeclaration {
        private final Key key;
        private Map<Method, List<Class<? extends MethodInterceptor>>> interceptors =
                new TreeMap<>(Comparator.comparing(Method::getName)
                        .thenComparing(Method::getParameterCount)
                        .thenComparing(it -> it.getParameterTypes().length == 0 ? "A"
                                : it.getParameterTypes()[0].getSimpleName()));

        AopDeclaration(final Key key) {
            this.key = key;
        }

        Key getKey() {
            return key;
        }

        Map<Method, List<Class<? extends MethodInterceptor>>> getInterceptors() {
            return interceptors;
        }
    }
}
