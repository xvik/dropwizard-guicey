package ru.vyarus.dropwizard.guice.debug.report.guice;

import com.google.common.base.Preconditions;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.spi.Elements;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ru.vyarus.dropwizard.guice.GuiceyOptions;
import ru.vyarus.dropwizard.guice.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.debug.report.guice.model.BindingDeclaration;
import ru.vyarus.dropwizard.guice.debug.report.guice.model.DeclarationType;
import ru.vyarus.dropwizard.guice.debug.report.guice.model.ModuleDeclaration;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.GuiceModelParser;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.GuiceModelUtils;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.debug.util.TreeNode;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Renders guice bindings as modules tree. Bindings information is analyzed using guice SPI directly from modules
 * in order to see all listeners, scopes and aop registrations. Report identifies bindings overrides.
 * <p>
 * Report use stack trace element convention for binding declaration in order to let intellij idea (or other tools)
 * to automatically apply link to source code.
 * <p>
 * Report sections:
 * <ul>
 * <li>Bindings from declared modules (as tree)</li>
 * <li>Bindings from overriding modules (as tree)</li>
 * <li>JIT bindings (if any)</li>
 * <li>Binding chains (to see actual binding resolution path)</li>
 * </ul>
 * <p>
 * Used markers:
 * <ul>
 * <li>EXTENSION - extension binding</li>
 * <li>REMOVED - extension or module disabled and binding(s) removed</li>
 * <li>AOP - bean affected by guice AOP</li>
 * <li>OVERRIDDEN (only in modules tree) - binding is overridden with binding from overriding module</li>
 * <li>OVERRIDES (only in overriding modules tree) - binding override something in main modules</li>
 * <li>WEB - indicates guice {@link com.google.inject.servlet.ServletModule}s</li>
 * <li>PRIVATE - indicates private module</li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 13.08.2019
 */
@SuppressWarnings("PMD.GodClass")
public class GuiceBindingsRenderer implements ReportRenderer<GuiceConfig> {

    private static final String REMOVED = "REMOVED";

    private final Injector injector;
    private final List<Module> modules;
    private final List<Module> overridden;
    private final List<Class<Object>> extensions;
    private final List<Class<Object>> disabled;
    private final List<Class<Module>> modulesDisabled;
    private final boolean analysisEnabled;

    /**
     * Create bindings renderer.
     *
     * @param injector injector
     */
    public GuiceBindingsRenderer(final Injector injector) {
        this.injector = injector;
        final GuiceyConfigurationInfo info = injector.getInstance(GuiceyConfigurationInfo.class);
        this.modules = info.getNormalModuleIds().stream()
                .map(it -> info.getData().<ModuleItemInfo>getInfo(it).getInstance())
                .collect(Collectors.toList());
        this.overridden = info.getOverridingModuleIds().stream()
                .map(it -> info.getData().<ModuleItemInfo>getInfo(it).getInstance())
                .collect(Collectors.toList());
        // important to highlight all extensions, including disabled
        this.extensions = ItemId.typesOnly(info.getData().getItems(ConfigItem.Extension));
        this.disabled = info.getExtensionsDisabled();
        // when module analysis disabled show entire context (because nothing would be removed)
        this.analysisEnabled = info.getOptions().getValue(GuiceyOptions.AnalyzeGuiceModules);
        this.modulesDisabled = analysisEnabled ? info.getModulesDisabled() : Collections.emptyList();
    }

    @Override
    public String renderReport(final GuiceConfig config) {
        // analyze modules
        final List<ModuleDeclaration> moduleItems = filter(
                GuiceModelParser.parse(injector, Elements.getElements(Stage.TOOL, modules)), config);
        final Map<Key, BindingDeclaration> moduleBindings = GuiceModelUtils.index(moduleItems);

        // don't show extensions if no guice module analysis actually performed
        if (analysisEnabled) {
            markExtensions(moduleBindings);
        }

        // analyze overrides
        final List<ModuleDeclaration> overrideItems = filter(overridden.isEmpty()
                ? Collections.emptyList() : GuiceModelParser.parse(injector,
                Elements.getElements(Stage.TOOL, overridden)), config);
        final Map<Key, BindingDeclaration> overrideBindings = GuiceModelUtils.index(overrideItems);

        markOverrides(moduleBindings, overrideBindings);

        final StringBuilder res = new StringBuilder();
        res.append(Reporter.NEWLINE).append(Reporter.NEWLINE);

        // put all known bindings together for remaining reports
        renderModules(res, moduleItems, moduleBindings);
        renderOverrides(res, overrideItems, overrideBindings);
        moduleBindings.putAll(overrideBindings);

        renderJitBindings(res, moduleBindings, config, extensions);
        renderBindingChains(res, moduleBindings);
        return res.toString();
    }

    private void renderModules(final StringBuilder res,
                               final List<ModuleDeclaration> moduleItems,
                               final Map<Key, BindingDeclaration> moduleBindings) {
        // never empty because of guicey module
        final TreeNode root = new TreeNode("%s MODULES with %s bindings",
                GuiceModelUtils.getModules(moduleItems).size(), moduleBindings.size());

        for (ModuleDeclaration mod : moduleItems) {
            render(root, mod);
        }
        root.render(res);
    }

    private void renderOverrides(final StringBuilder res,
                                 final List<ModuleDeclaration> overrideItems,
                                 final Map<Key, BindingDeclaration> overrideBindings) {
        if (!overrideItems.isEmpty()) {
            final TreeNode root = new TreeNode("%s OVERRIDING MODULES with %s bindings",
                    GuiceModelUtils.getModules(overrideItems).size(), overrideBindings.size());
            for (ModuleDeclaration mod : overrideItems) {
                render(root, mod);
            }
            if (root.hasChildren()) {
                res.append(Reporter.NEWLINE).append(Reporter.NEWLINE);
                root.render(res);
            }
        }
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
    private void renderJitBindings(final StringBuilder res,
                                   final Map<Key, BindingDeclaration> moduleBindings,
                                   final GuiceConfig config,
                                   final List<Class<Object>> extensions) {
        // all bindings contains not only all declared + pure jit bindings, but also second sides for providerkey
        // and linkkey bindings. providerkeys are filtered automatically and link key removed below
        final Map<Key<?>, Binding<?>> jitBindings = new HashMap<>(injector.getAllBindings());
        // remove non JIT bindings (from guice point of view).. there will be some technical bindings
        // not mentioned in report (usually right parts of declarations)
        for (Key<?> key : injector.getBindings().keySet()) {
            jitBindings.remove(key);
        }
        for (BindingDeclaration dec : moduleBindings.values()) {
            // remove extension bindings (for example, servlet or filter extensions)
            if (dec.getKey() != null) {
                jitBindings.remove(dec.getKey());
            }
            // remove "JIT" bindings from key declaration: bind(A.class).to(B.class) will lead to jit binding creation
            // for B class, but we don't need to know about it - only pure JIT bindings required
            if (dec.getTarget() != null) {
                jitBindings.remove(dec.getTarget());
            }
        }
        // remove extension bindings (may remain from complex mappings like plugins)
        jitBindings.keySet().removeIf(key -> extensions.contains(key.getTypeLiteral().getRawType()));
        if (!jitBindings.isEmpty()) {
            final List<ModuleDeclaration> jits = filter(GuiceModelParser.parse(injector, jitBindings.values()), config);
            if (!jits.isEmpty()) {
                Preconditions.checkState(jits.size() == 1, "One module expected, but got %s", jits.size());
                final List<BindingDeclaration> declarations = jits.get(0).getDeclarations();
                final TreeNode root = new TreeNode("%s UNDECLARED bindings", declarations.size());
                for (BindingDeclaration dec : declarations) {
                    if (dec.getKey() != null) {
                        // this could be constant conversion bindings, which should be shown in chains report
                        moduleBindings.put(dec.getKey(), dec);
                    }
                    root.child(String.format("%-28s %-26s", renderElement(dec),
                            RenderUtils.brackets(RenderUtils.renderPackage(dec.getKey().getTypeLiteral().getRawType())))
                    );
                }
                if (root.hasChildren()) {
                    res.append(Reporter.NEWLINE).append(Reporter.NEWLINE);
                    root.render(res);
                }
            }
        }
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
    private void renderBindingChains(final StringBuilder res,
                                     final Map<Key, BindingDeclaration> bindings) {
        final List<BindingDeclaration> chains = new ArrayList<>();
        final List<Key> targetKeys = new ArrayList<>();
        for (BindingDeclaration dec : bindings.values()) {
            // do not ignore disabled bindings to show entire removed chains

            // select only links because direct providers are easily visible on main tree
            if (dec.getTarget() != null) {
                chains.add(dec);
                if (dec.getTarget() != null) {
                    targetKeys.add(dec.getTarget());
                }
            }
        }
        // filter middle elements
        chains.removeIf(it -> targetKeys.contains(it.getKey()));

        final List<String> lines = renderChainLines(chains, bindings);
        if (!lines.isEmpty()) {
            // sort by name
            lines.sort(Comparator.naturalOrder());
            final TreeNode root = new TreeNode("BINDING CHAINS", chains.size());
            for (String line : lines) {
                root.child(line);
            }

            res.append(Reporter.NEWLINE).append(Reporter.NEWLINE);
            root.render(res);
        }
    }

    private List<ModuleDeclaration> filter(final List<ModuleDeclaration> modules, final GuiceConfig config) {
        modules.removeIf(it -> config.getIgnoreModules().contains(it.getType())
                || (!it.isJitModule() && filter(it.getType().getName(), config.getIgnorePackages())));
        for (ModuleDeclaration mod : modules) {
            if (modulesDisabled.contains(mod.getType())) {
                // ignore removed module's subtree
                mod.getDeclarations().clear();
                mod.getMarkers().add(REMOVED);
                mod.getChildren().clear();
            } else {
                mod.getDeclarations().removeIf(it -> it.getKey() != null && filter(
                        it.getKey().getTypeLiteral().getRawType().getName(), config.getIgnorePackages()));
            }
            filter(mod.getChildren(), config);
        }
        return modules;
    }

    private boolean filter(final String type, final List<String> pkgs) {
        for (String pkg : pkgs) {
            if (type.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private void markOverrides(final Map<Key, BindingDeclaration> moduleBindings,
                               final Map<Key, BindingDeclaration> overrideBindings) {
        if (!overrideBindings.isEmpty()) {
            for (Map.Entry<Key, BindingDeclaration> entry : overrideBindings.entrySet()) {
                final Key key = entry.getKey();
                final BindingDeclaration dec = moduleBindings.get(key);
                if (dec != null) {
                    dec.getMarkers().add("OVERRIDDEN");
                    entry.getValue().getMarkers().add("OVERRIDE");
                }
            }
        }
    }

    private void markExtensions(final Map<Key, BindingDeclaration> moduleBindings) {
        for (BindingDeclaration dec : moduleBindings.values()) {
            if (extensions.contains(dec.getKey().getTypeLiteral().getRawType())) {
                dec.getMarkers().add("EXTENSION");
            }
        }
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
    private void render(final TreeNode root, final ModuleDeclaration mod) {
        final TreeNode next = root.child(RenderUtils.renderClassLine(mod.getType(), mod.getMarkers()));

        for (BindingDeclaration dec : mod.getDeclarations()) {
            if (analysisEnabled && isDisabledBinding(dec)) {
                dec.getMarkers().add(REMOVED);
            }
            final String type = dec.getType().isRuntimeBinding()
                    ? dec.getType().name().toLowerCase() : "<" + dec.getType().name().toLowerCase() + ">";
            final StringBuilder msg = new StringBuilder(String.format("%-20s %-16s %-45s",
                    type,
                    dec.getScope() != null ? "[@" + dec.getScope().getSimpleName() + "]" : "",
                    renderElement(dec)));
            msg.append("   at ").append(dec.getSource());
            if (!dec.getMarkers().isEmpty()) {
                msg.append(' ').append(RenderUtils.markers(dec.getMarkers()));
            }
            next.child(msg.toString());
        }

        for (ModuleDeclaration child : mod.getChildren()) {
            render(next, child);
        }
    }

    private boolean isDisabledBinding(final BindingDeclaration dec) {
        return (dec.getKey() != null && disabled.contains(dec.getKey().getTypeLiteral().getRawType()))
                // linked keys may be disabled by target too
                || (dec.getType() == DeclarationType.LinkedKey
                && disabled.contains(dec.getTarget().getTypeLiteral().getRawType()));
    }

    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    private String renderElement(final BindingDeclaration declaration) {
        String res;
        if (declaration.getKey() == null && declaration.getSpecial() != null) {
            // special binding without key (listeners, scopes)
            res = declaration.getSpecial().stream()
                    .map(it -> RenderUtils.getClassName(it.getClass()))
                    .collect(Collectors.joining(","));

        } else {
            res = GuiceModelUtils.renderKey(declaration.getKey());
            // for linked key important to show target (more obvious)
            if (declaration.getType() == DeclarationType.LinkedKey && declaration.getTarget() != null) {
                res += " --> " + GuiceModelUtils.renderKey(declaration.getTarget());
            }
            if (declaration.getSpecial() != null) {
                res += " (" + declaration.getSpecial()
                        .stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
            }
        }
        return res;
    }

    @SuppressWarnings({"unchecked", "PMD.CognitiveComplexity"})
    private List<String> renderChainLines(final List<BindingDeclaration> roots,
                                          final Map<Key, BindingDeclaration> bindings) {
        final List<String> lines = new ArrayList<>();
        for (BindingDeclaration dec : roots) {
            final StringBuilder line = new StringBuilder(GuiceModelUtils.renderKey(dec.getKey()));
            BindingDeclaration curr = dec;
            while (curr != null && (curr.getTarget() != null || curr.getProvidedBy() != null)) {
                final Key link = curr.getTarget();
                if (link != null) {
                    final String tag = curr.getType().equals(DeclarationType.ConvertedConstant)
                            ? "converted" : "linked";
                    line.append("  --[").append(tag).append("]-->  ").append(GuiceModelUtils.renderKey(link));
                } else {
                    line.append("  --[provided]-->  ").append(curr.getProvidedBy());
                }

                BindingDeclaration nextDec = null;
                if (link != null) {
                    nextDec = bindings.get(link);
                    if (nextDec == null) {
                        // bindings map may contain not all bindings due to filtering, but here we need
                        // to show entire chain
                        final Binding existingBinding = injector.getExistingBinding(link);
                        if (existingBinding != null) {
                            nextDec = GuiceModelParser.parseElement(injector, existingBinding);
                        } else {
                            line.append("       *CHAIN REMOVED");
                        }
                    }
                }
                curr = nextDec;
            }
            lines.add(line.toString());
        }
        return lines;
    }
}
