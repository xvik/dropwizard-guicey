package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.internal.MoreTypes;
import com.google.inject.internal.PrivateElementsImpl;
import com.google.inject.internal.Scoping;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.PrivateElements;
import com.google.inject.spi.UntargettedBinding;
import com.google.inject.util.Modules;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceyOptions;
import ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.stat.Stat;
import ru.vyarus.dropwizard.guice.module.context.stat.StatTimer;
import ru.vyarus.dropwizard.guice.module.installer.util.BindingUtils;
import ru.vyarus.dropwizard.guice.module.support.BootstrapAwareModule;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationTreeAwareModule;
import ru.vyarus.dropwizard.guice.module.support.EnvironmentAwareModule;
import ru.vyarus.dropwizard.guice.module.support.OptionsAwareModule;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.vyarus.dropwizard.guice.GuiceyOptions.AnalyzeGuiceModules;
import static ru.vyarus.dropwizard.guice.GuiceyOptions.InjectorStage;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.InstallersTime;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.ModulesProcessingTime;

/**
 * Helper class for guice modules processing.
 *
 * @author Vyacheslav Rusakov
 * @since 25.04.2018
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.GodClass", "ClassFanOutComplexity", "PMD.CouplingBetweenObjects"})
public final class ModulesSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModulesSupport.class);

    private ModulesSupport() {
    }

    /**
     * Post-process registered modules by injecting bootstrap, configuration, environment and options objects.
     *
     * @param context configuration context
     */
    @SuppressWarnings("unchecked")
    public static void configureModules(final ConfigurationContext context) {
        for (Module mod : context.getEnabledModules()) {
            if (mod instanceof BootstrapAwareModule) {
                ((BootstrapAwareModule) mod).setBootstrap(context.getBootstrap());
            }
            if (mod instanceof ConfigurationAwareModule) {
                ((ConfigurationAwareModule) mod).setConfiguration(context.getConfiguration());
            }
            if (mod instanceof ConfigurationTreeAwareModule) {
                ((ConfigurationTreeAwareModule) mod).setConfigurationTree(context.getConfigurationTree());
            }
            if (mod instanceof EnvironmentAwareModule) {
                ((EnvironmentAwareModule) mod).setEnvironment(context.getEnvironment());
            }
            if (mod instanceof OptionsAwareModule) {
                ((OptionsAwareModule) mod).setOptions(context.optionsReadOnly());
            }
        }
    }

    /**
     * Prepares modules to use for injector creation (applies module overrides).
     *
     * @param context configuration context
     * @return modules for injector creation
     */
    public static Iterable<Module> prepareModules(final ConfigurationContext context) {
        final StatTimer timer = context.stat().timer(ModulesProcessingTime);
        final List<Module> overridingModules = context.getOverridingModules();
        // repackage normal modules to reveal all guice extensions
        final List<Module> normalModules = analyzeModules(context, timer);

        final Iterable<Module> res = overridingModules.isEmpty() ? normalModules
                : Collections.singletonList(Modules.override(normalModules).with(overridingModules));
        timer.stop();
        return res;
    }

    /**
     * Search for extensions in guice bindings (directly declared in modules).
     * Only user provided modules are analyzed. Overriding modules are not analyzed.
     * <p>
     * Use guice SPI. In order to avoid duplicate analysis in injector creation time, wrap
     * parsed elements as new module (and use it instead of original modules). Also, if
     * bound extension is disabled, target binding is simply removed (in order to
     * provide the same disable semantic as with usual extensions).
     *
     * @param context configuration context
     * @param modulesTimer modules processing timer
     * @return list of repackaged modules to use
     */
    private static List<Module> analyzeModules(final ConfigurationContext context,
                                               final StatTimer modulesTimer) {
        List<Module> modules = context.getNormalModules();
        final Boolean configureFromGuice = context.option(AnalyzeGuiceModules);
        // one module mean no user modules registered
        if (modules.size() > 1 && configureFromGuice) {
            // analyzing only user bindings (excluding overrides and guicey technical bindings)
            final GuiceBootstrapModule bootstrap = (GuiceBootstrapModule) modules.remove(modules.size() - 1);
            try {
                // find extensions and remove bindings if required (disabled extensions)
                final StatTimer gtime = context.stat().timer(Stat.BindingsResolutionTime);
                final List<Element> elements = new ArrayList<>(
                        Elements.getElements(context.option(InjectorStage), modules));
                gtime.stop();

                // exclude analysis time from modules processing time (it's installer time)
                modulesTimer.stop();
                analyzeAndFilterBindings(context, modules, elements);
                modulesTimer.start();

                // wrap raw elements into module to avoid duplicate work on guice startup and put back bootstrap
                modules = Arrays.asList(Elements.getModule(elements), bootstrap);
            } catch (Exception ex) {
                // better show meaningful message then just fail entire startup with ambiguous message
                // NOTE if guice configuration is not OK it will fail here too, but user will see injector creation
                // error as last error in logs.
                LOGGER.error("Failed to analyze guice bindings - skipping this step. Note that configuration"
                        + " from bindings may be switched off with " + GuiceyOptions.class.getSimpleName() + "."
                        + AnalyzeGuiceModules.name() + " option.", ex);
                // recover and use original modules
                modules.add(bootstrap);
                if (!modulesTimer.isRunning()) {
                    modulesTimer.start();
                }
            }
        }
        return modules;
    }

    private static void analyzeAndFilterBindings(final ConfigurationContext context,
                                                 final List<Module> analyzedModules,
                                                 final List<Element> elements) {
        final StatTimer itimer = context.stat().timer(InstallersTime);
        final StatTimer timer = context.stat().timer(Stat.ExtensionsRecognitionTime);
        final StatTimer analysisTimer = context.stat().timer(Stat.BindingsAnalysisTime);
        final List<String> disabledModules = prepareDisabledModules(context);

        final AnalysisResult result = analyzeElements(context, elements, disabledModules, null);

        if (!result.actuallyDisabledModules.isEmpty()) {
            LOGGER.debug("Removed inner guice modules: {}", result.actuallyDisabledModules);
        }
        // must be triggered only once (at the end of overall analysis)
        context.stat().count(Stat.RemovedInnerModules, result.actuallyDisabledModules.size());
        context.stat().count(Stat.RemovedBindingsCount, result.removedBindings.size());
        context.lifecycle().modulesAnalyzed(analyzedModules, result.extensions,
                toModuleClasses(result.actuallyDisabledModules), result.removedBindings);
        analysisTimer.stop();
        timer.stop();
        itimer.stop();
    }

    /**
     * Actual guice modules analysis. Would be called for all guice modules and for each private module
     * (because private module bindings are managed independently).
     * <p>
     * Bindings for disabled extensions would be removed.
     *
     * @param context         guicey context
     * @param elements        parsed guice module elements (all bindings)
     * @param disabledModules disabled modules
     * @param privateFilter   custom extensions filter used for private modules analysis (only exposed bindings could be
     *                        accepted)
     * @return analysis report (with found extensions and removed bindings)
     */
    private static AnalysisResult analyzeElements(final ConfigurationContext context,
                                                  final List<Element> elements,
                                                  final List<String> disabledModules,
                                                  final Predicate<Key> privateFilter) {
        context.stat().count(Stat.BindingsCount, elements.size());

        final Set<String> actuallyDisabledModules = new HashSet<>();
        final List<Binding> removedBindings = new ArrayList<>();
        final List<Class<?>> extensions = new ArrayList<>();
        // extension may be recognized by linked key and linked keys may need to be removed too
        // linked key -> binding
        final Multimap<Key, LinkedKeyBinding> linkedBindings = LinkedHashMultimap.create();

        final Iterator<Element> it = elements.iterator();
        while (it.hasNext()) {
            final Element element = it.next();
            if (isInDisabledModule(element, disabledModules, actuallyDisabledModules)) {
                // remove all bindings under disabled modules
                it.remove();
                context.stat().count(Stat.RemovedBindingsCount, 1);
                continue;
            }
            // filter constants, listeners, aop etc.
            if (element instanceof Binding
                    && detectExtensionAndRemoveBindingIfDisabled(context, (Binding) element,
                    extensions, linkedBindings, privateFilter)) {
                it.remove();
                removedBindings.add((Binding) element);
            } else if (element instanceof PrivateElements
                    // no need to analyze private modules inside private modules - no services could be exposed
                    && privateFilter == null && (boolean) context.option(GuiceyOptions.AnalyzePrivateGuiceModules)) {
                // private module is a child injector with only some bindings exposed to the parent - processing it
                // as a separate process (private moduel is an independent set of bindings)
                final AnalysisResult result = analyzePrivateModule((PrivateElements) element,
                        context, disabledModules);
                extensions.addAll(result.extensions);
                removedBindings.addAll(result.removedBindings);
                actuallyDisabledModules.addAll(result.actuallyDisabledModules);
                // linkedBindings not added because they would be already processed for private module
            }
        }
        // Recognize extensions in linked bindings and remove bindings for disabled extensions (entire chains)
        // Could not be done earlier as entire chains must be analyzed
        for (Binding binding : detectExtensionsInLinkedBindingsAndRemoveDisabled(
                context, extensions, linkedBindings, privateFilter)) {
            elements.remove(binding);
            removedBindings.add(binding);
        }
        return new AnalysisResult(extensions, removedBindings, actuallyDisabledModules);
    }

    /**
     * Checks if provided binding key is a guicey extension. If detected extension is disabled, remove binding (note
     * that related linked binding would be cleared later).
     * <p>
     * If provided binding is linked binding - just register it. It can't be analyzed right ahead because linked
     * bindings could for a chain and we should detect top-most class as an extension (without it, multiple classes
     * would be registered for the same binding).
     *
     * @param context        guicey context
     * @param binding        binding to analyze
     * @param extensions     list of already detected extensions
     * @param linkedBindings already discovered linked bindings map
     * @param privateFilter  extension fileter for private module (because only exposed bindings could be analyzed)
     * @return true if binding was removed (for disabled extension), false otherwise
     */
    private static boolean detectExtensionAndRemoveBindingIfDisabled(
            final ConfigurationContext context,
            final Binding binding,
            final List<Class<?>> extensions,
            final Multimap<Key, LinkedKeyBinding> linkedBindings,
            final Predicate<Key> privateFilter) {
        final Key key = binding.getKey();
        if (isPossibleExtension(key, privateFilter)) {
            context.stat().count(Stat.AnalyzedBindingsCount, 1);
            final Class type = key.getTypeLiteral().getRawType();
            if (context.isAcceptableAutoScanClass(type)
                    && ExtensionsSupport.registerExtensionBinding(context, type,
                    binding, BindingUtils.getTopDeclarationModule(binding))) {
                LOGGER.debug("Extension detected from guice binding: {}", type.getSimpleName());
                extensions.add(type);
                return !context.isExtensionEnabled(type);
            }
        }
        // note if linked binding recognized as extension by its key - it would not be counted (not needed)
        if (binding instanceof LinkedKeyBinding) {
            // remember all linked bindings (do not recognize on first path to avoid linked binding check before
            // real binding)
            final LinkedKeyBinding linkedBind = (LinkedKeyBinding) binding;
            linkedBindings.put(linkedBind.getLinkedKey(), linkedBind);
        }
        return false;
    }

    private static boolean isPossibleExtension(final Key key, final Predicate<Key> filter) {
        // extension bindings may be only unqualified
        return key.getAnnotation() == null
                // class only (no generified types)
                && key.getTypeLiteral().getType() instanceof Class
                // additional filter to check for other conditions (used for private modules)
                && (filter == null || filter.test(key));
    }

    // links map is: linked type (end) -> binding
    private static List<LinkedKeyBinding> detectExtensionsInLinkedBindingsAndRemoveDisabled(
            final ConfigurationContext context,
            final List<Class<?>> extensions,
            final Multimap<Key, LinkedKeyBinding> links,
            final Predicate<Key> privateFilter) {
        // try to recognize extensions in links
        for (Map.Entry<Key, LinkedKeyBinding> entry : links.entries()) {
            final Key key = entry.getKey();
            final Class type = key.getTypeLiteral().getRawType();
            final LinkedKeyBinding binding = entry.getValue();
            if (!isPossibleExtension(key, privateFilter)) {
                continue;
            }
            // try to detect extension in linked type (binding already analyzed so no need to count)
            if (!extensions.contains(type)
                    && context.isAcceptableAutoScanClass(type)
                    && ExtensionsSupport.registerExtensionBinding(context, type,
                    binding, BindingUtils.getTopDeclarationModule(binding))) {
                LOGGER.debug("Extension detected from guice linked binding: {}", type.getSimpleName());
                extensions.add(type);
            }
        }
        // remove linked bindings for disabled extensions (entire chains)
        final List<Key> removedExtensions = extensions.stream()
                .filter(it -> !context.isExtensionEnabled(it))
                .map(Key::get)
                .collect(Collectors.toList());
        return removeChains(removedExtensions, links);
    }

    /**
     * Pass in removed binding keys. Need to find all links ending on removed type and remove.
     * Next, repeat with just removed types (to clean up entire chains because with it context may not start).
     * For example: {@code bind(Interface1).to(Interface2); bind(Interface2).to(Extension)}
     * Extension detected as extension, but if its disabled then link (Interface2 -> Extension) must be removed
     * but without it Interface1 -> Interface2 remains and fail context because its just interfaces
     * that's why entire chains must be removed.
     *
     * @param removed  removed keys (to clean links leading to these keys)
     * @param bindings all linked bindings (actually without links with recognized extension in left part)
     * @return list of bindings to remove
     */
    private static List<LinkedKeyBinding> removeChains(final List<Key> removed,
                                                       final Multimap<Key, LinkedKeyBinding> bindings) {
        final List<Key> newlyRemoved = new ArrayList<>();
        final List<LinkedKeyBinding> res = new ArrayList<>();

        for (Key removedKey : removed) {
            // remove all links ending on removed key
            for (LinkedKeyBinding bnd : bindings.get(removedKey)) {
                res.add(bnd);
                newlyRemoved.add(bnd.getKey());
            }
        }

        // continue removing chains
        if (!newlyRemoved.isEmpty()) {
            res.addAll(removeChains(newlyRemoved, bindings));
        }
        return res;
    }

    /**
     * Search extensions in private module. In contrast to the usual module, in private module we could see only
     * exposed bindings, and so we analyze just directly exposed bindings and linked chains from exposed bindings.
     * For example: {@code expose(Ext)} - directly exposed extension, and {@code bind(Intrce).to(Ext); expose(Intrce)}
     * - indirectly exposed extension, which still could be obtained through interface.
     * <p>
     * Guicey extensions assumed to be resolved by class (you can get extension instance from guice context using
     * extension class {@code injector.getInstance(Ext)}). In case of indirect exposure from private module extension
     * would not be accessible by class. I did not want to complicate guicey introducing new "binding" key concept
     * for extensions (assuming so many installers already rely on direct extension getting from injector).
     * To work around this, guicey would ADD custom extension bindings (if required) and expose extension directly.
     * This would not be visible in reports.
     * <p>
     * Note that there would not be a "same class in two private modules" problem, because guicey counts only
     * exposed extensions and so you will not be able to expose the same class twice into parent context.
     * <p>
     * Disabling extensions will also lead to bindings remove in private module (this is possible because
     * private module elements are MUTABLE (to some point) and so could be modified). In case of disabled module,
     * all private module bindings would be removed. But it is also possible to remove sub module inside private
     * module (but only first level!).
     *
     * @param privateModule   private module
     * @param context         guicey context
     * @param disabledModules disabled modules
     * @return module analysis report (all findings must be added to the main analysis report)
     */
    private static AnalysisResult analyzePrivateModule(final PrivateElements privateModule,
                                                       final ConfigurationContext context,
                                                       final List<String> disabledModules) {
        final PrivateElementsImpl module = (PrivateElementsImpl) privateModule;

        final PrivateExposedFilter filter = new PrivateExposedFilter(module);

        // IMPORTANT using mutable elements to be able to modify bindings (100% legal - gucie do it internally)
        // After getElements() call, all further mutable collection changes would be ignored!
        // Another moment: it would be possible to remove sub-modules (no matter that they are private)
        final AnalysisResult result = analyzeElements(context, module.getElementsMutable(), disabledModules, filter);

        // missed bindings (not directly exposed extensions)
        final List<Class> toExpose = new ArrayList<>();
        for (Class<?> ext : result.extensions) {
            // do not expose disabled extensions
            if (context.isExtensionEnabled(ext)) {
                final Key expose = Preconditions.checkNotNull(filter.detected.get(ext),
                        "Exposed key not found for detected extension : %s", ext.getName());
                if (!expose.getTypeLiteral().getRawType().equals(ext)) {
                    LOGGER.debug(
                            "Extension {} is indirectly exposed from private module by key {}. Adding direct expose",
                            ext, expose);
                    toExpose.add(ext);
                }
            }
        }
        exposeIndirectPrivateModuleExtensions(toExpose, filter.boundKeys, result.removedBindings, module);

        return result;
    }

    // registering additional expose keys, so extensions become available by class directly (required)
    // Implementation hack: have to override existing immutable map with reflection
    // (note that new exposures would not be visible in the report because it analyzes modules!)

    /**
     * <code>
     * bind(Iface.class).to(Ext.class)
     * expose(IFace.class)
     * </code>
     * For extensions detected in the exposed chain, but not directly exposed, direct expose is required.
     * But expose requires existing binding: in the example above {@code expose(Ext.class)} would not work
     * as guice requires explicit binding (at least, untargetted {@code bind(Ext.class)}). So guice would not only
     * add expose for extension, but will register untargetted binding if there is no existing binding for extension.
     * <p>
     * Also, generic analysis logic removes only bindings for disabled extensions, but an extension key will remain
     * as exposed - it must be also removed.
     *
     * @param notExposed      detected private module extensions without expose
     * @param boundKeys       all binging keys, used in module
     * @param removedBindings removed bindings (required to remove exposed keys)
     * @param module          private module element (to modify)
     */
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    private static void exposeIndirectPrivateModuleExtensions(final List<Class> notExposed,
                                                              final Set<Key<?>> boundKeys,
                                                              final List<Binding> removedBindings,
                                                              final PrivateElementsImpl module) {
        // need to add expose and untargetted bindings to be able to access extension by extension class directly
        try {

            // exposed bindings field (can't access it in a "normal way" at this stage)
            final Field exposedKeysField = PrivateElementsImpl.class.getDeclaredField("exposedKeysToSources");
            exposedKeysField.setAccessible(true);
            final Map<Key<?>, Object> exposedKeys = new HashMap<>((Map<Key<?>, Object>) exposedKeysField.get(module));
            // existing exposes for removed bindings must be cleared
            for (Binding binding : removedBindings) {
                exposedKeys.remove(binding.getKey());
                if (binding instanceof LinkedKeyBinding<?>) {
                    exposedKeys.remove(((LinkedKeyBinding<?>) binding).getLinkedKey());
                }
            }

            // package private - no way to use directly
            final Constructor ctor = Class.forName("com.google.inject.internal.UntargettedBindingImpl")
                    .getDeclaredConstructor(Object.class, Key.class, Scoping.class);
            ctor.setAccessible(true);

            // add missed keys
            for (Class ext : notExposed) {
                LOGGER.debug("Registering synthetic bindings for private module to expose extension: {}",
                        ext.getName());
                final Key key = MoreTypes.canonicalizeKey(Key.get(ext));
                if (!boundKeys.contains(key)) {
                    // add required untargetted binding for added expose
                    module.getElementsMutable().add((UntargettedBinding) ctor.newInstance(
                            "Synthetic binding to expose extension directly: " + ext.getName(), key, Scoping.UNSCOPED));
                }
                exposedKeys.put(key, "Synthetic exposure for extension: " + ext.getName());
            }

            exposedKeysField.set(module, ImmutableMap.copyOf(exposedKeys));
            exposedKeysField.setAccessible(false);
            ctor.setAccessible(false);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to override exposed keys for private module", e);
        }
    }

    /**
     * Find (possible) exposed key in the linked bindings chain. In the simplest case - key itself.
     *
     * @param key            key to check
     * @param linkedBindings all linked bindings in private module
     * @param exposedKeys    all exposed keys in private module
     * @return exposed key (could be provided key itself) or null if key unreachable outside private module
     */
    private static Key findExposed(final Key key,
                                   final Multimap<Key, LinkedKeyBinding> linkedBindings,
                                   final Set<Key<?>> exposedKeys) {
        Key res = exposedKeys.contains(key) ? key : null;
        if (res == null) {
            for (LinkedKeyBinding target : linkedBindings.get(key)) {
                final Key exposed = findExposed(target.getKey(), linkedBindings, exposedKeys);
                if (exposed != null) {
                    res = exposed;
                    break;
                }
            }
        }
        return res;
    }

    private static List<String> prepareDisabledModules(final ConfigurationContext context) {
        final List<String> res = new ArrayList<>();
        for (Class cls : context.getDisabledModuleTypes()) {
            res.add(cls.getName());
        }
        return res;
    }

    private static boolean isInDisabledModule(final Element element,
                                              final List<String> disabled,
                                              final Set<String> actuallyDisabled) {
        if (!disabled.isEmpty()) {
            final List<String> modules = BindingUtils.getModules(element);
            // need to check from top modules to lower, otherwise removed modules list will be incorrect
            for (int i = modules.size() - 1; i >= 0; i--) {
                final String mod = modules.get(i);
                if (disabled.contains(mod)) {
                    actuallyDisabled.add(mod);
                    return true;
                }
            }
        }
        return false;
    }

    private static List<Class<? extends Module>> toModuleClasses(final Set<String> modules) {
        if (modules.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Class<? extends Module>> res = new ArrayList<>();
        for (String mod : modules) {
            res.add(BindingUtils.getModuleClass(mod));
        }
        return res;
    }

    /**
     * Guice modules analysis result.
     */
    @SuppressWarnings("VisibilityModifier")
    private static class AnalysisResult {
        public final List<Class<?>> extensions;
        public final List<Binding> removedBindings;
        public final Set<String> actuallyDisabledModules;

        AnalysisResult(final List<Class<?>> extensions,
                       final List<Binding> removedBindings,
                       final Set<String> actuallyDisabledModules) {
            this.extensions = extensions;
            this.removedBindings = removedBindings;
            this.actuallyDisabledModules = actuallyDisabledModules;
        }
    }

    /**
     * Extensions filter for private modules: only exposed extensions, or extensions in the exposed chain could
     * be accepted.
     * <p>
     * For example:
     * <code>
     * bind(Iface.class).to(Ext.class)
     * expose(Iface.class)
     * </code>
     * Here only Iface is exposed, but it provides access for actual Ext class and so extension should be considered
     * as acceptable.
     */
    @SuppressWarnings("VisibilityModifier")
    private static class PrivateExposedFilter implements Predicate<Key> {

        public final Set<Key<?>> boundKeys;
        // collect exposure keys for extension (important for not directly exposed extensions detection)
        public final Map<Class, Key> detected = new HashMap<>();

        // all linked bindings in private module so we could track the entire chains
        // linked key - binding (to easily go in backwards direction)
        private final Multimap<Key, LinkedKeyBinding> linkedBindings;
        // all exposed keys
        private final Set<Key<?>> exposedKeys;

        PrivateExposedFilter(final PrivateElementsImpl module) {
            exposedKeys = module.getExposedKeys();
            boundKeys = new HashSet<>();
            linkedBindings = LinkedHashMultimap.create();
            // have to collect links before analysis - otherwise exposure test filter couldn't work correctly
            for (final Element element : module.getElementsMutable()) {
                if (element instanceof Binding) {
                    boundKeys.add(((Binding) element).getKey());
                }
                if (element instanceof LinkedKeyBinding) {
                    final LinkedKeyBinding linkedBind = (LinkedKeyBinding) element;
                    linkedBindings.put(linkedBind.getLinkedKey(), linkedBind);
                }
            }
        }

        @Override
        public boolean test(final Key key) {
            final Key exposed = findExposed(key, linkedBindings, exposedKeys);
            if (exposed != null) {
                detected.put(key.getTypeLiteral().getRawType(), exposed);
            }
            return exposed != null;
        }
    }
}
