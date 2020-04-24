package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.common.base.Stopwatch;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.util.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceyOptions;
import ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.context.stat.Stat;
import ru.vyarus.dropwizard.guice.module.installer.util.BindingUtils;
import ru.vyarus.dropwizard.guice.module.support.*;

import java.util.*;
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
        final Options options = new Options(context.options());
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
                ((OptionsAwareModule) mod).setOptions(options);
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
        final Stopwatch timer = context.stat().timer(ModulesProcessingTime);
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
     * @return list of repackaged modules to use
     */
    private static List<Module> analyzeModules(final ConfigurationContext context,
                                               final Stopwatch modulesTimer) {
        List<Module> modules = context.getNormalModules();
        final Boolean configureFromGuice = context.option(AnalyzeGuiceModules);
        // one module mean no user modules registered
        if (modules.size() > 1 && configureFromGuice) {
            // analyzing only user bindings (excluding overrides and guicey technical bindings)
            final GuiceBootstrapModule bootstrap = (GuiceBootstrapModule) modules.remove(modules.size() - 1);
            try {
                // find extensions and remove bindings if required (disabled extensions)
                final Stopwatch gtime = context.stat().timer(Stat.BindingsResolutionTime);
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

    @SuppressWarnings("PMD.NcssCount")
    private static void analyzeAndFilterBindings(final ConfigurationContext context,
                                                 final List<Module> analyzedModules,
                                                 final List<Element> elements) {
        final Stopwatch itimer = context.stat().timer(InstallersTime);
        final Stopwatch timer = context.stat().timer(Stat.ExtensionsRecognitionTime);
        context.stat().count(Stat.BindingsCount, elements.size());
        final List<String> disabledModules = prepareDisabledModules(context);
        final Set<String> actuallyDisabledModules = new HashSet<>();
        final List<Binding> removedBindings = new ArrayList<>();
        final Iterator<Element> it = elements.iterator();
        final List<Class<?>> extensions = new ArrayList<>();
        // extension may be recognized by linked key and linked keys may need to be removed too
        // right key -> binding
        final Multimap<Key, LinkedKeyBinding> linkedBindings = LinkedHashMultimap.create();
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
                    && checkBindingRemoveRequired(context, (Binding) element, extensions, linkedBindings)) {
                it.remove();
                removedBindings.add((Binding) element);
            }
        }
        // recognize extensions in linked bindings and remove required bindings
        for (Binding binding : findLinkedBindingsToRemove(context, extensions, linkedBindings)) {
            elements.remove(binding);
            removedBindings.add(binding);
        }
        if (!actuallyDisabledModules.isEmpty()) {
            LOGGER.debug("Removed inner guice modules: {}", actuallyDisabledModules);
        }
        context.stat().count(Stat.RemovedInnerModules, actuallyDisabledModules.size());
        context.stat().count(Stat.RemovedBindingsCount, removedBindings.size());
        context.lifecycle().modulesAnalyzed(analyzedModules, extensions, toModuleClasses(actuallyDisabledModules),
                removedBindings);
        timer.stop();
        itimer.stop();
    }

    private static boolean checkBindingRemoveRequired(final ConfigurationContext context,
                                                      final Binding binding,
                                                      final List<Class<?>> extensions,
                                                      final Multimap<Key, LinkedKeyBinding> linkedBindings) {
        final Key key = binding.getKey();
        if (isPossibleExtension(key)) {
            context.stat().count(Stat.AnalyzedBindingsCount, 1);
            final Class type = key.getTypeLiteral().getRawType();
            if (ExtensionsSupport.registerExtensionBinding(context, type,
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

    // extension bindings may be only unqualified, class only (no generified types)
    private static boolean isPossibleExtension(final Key key) {
        return key.getAnnotation() == null && key.getTypeLiteral().getType() instanceof Class;
    }

    // links map is: linked type (end) -> binding
    private static List<LinkedKeyBinding> findLinkedBindingsToRemove(final ConfigurationContext context,
                                                                     final List<Class<?>> extensions,
                                                                     final Multimap<Key, LinkedKeyBinding> links) {
        // try to recognize extensions in links
        for (Map.Entry<Key, LinkedKeyBinding> entry : links.entries()) {
            final Key key = entry.getKey();
            final Class type = key.getTypeLiteral().getRawType();
            final LinkedKeyBinding binding = entry.getValue();
            if (!isPossibleExtension(key)) {
                continue;
            }
            // try to detect extension in linked type (binding already analyzed so no need to count)
            if (!extensions.contains(type) && ExtensionsSupport.registerExtensionBinding(context, type,
                    binding, BindingUtils.getTopDeclarationModule(binding))) {
                LOGGER.debug("Extension detected from guice link binding: {}", type.getSimpleName());
                extensions.add(type);
            }
        }
        // find disabled bindings (already removed)
        // for extensions recognized from links above imagine as we removed some (not existing) bindings
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
     * but without it Interface1 -> Interface2 remains and fail context becuase its just interfaces
     * that's why entire chains must be removed.
     *
     * @param removed  removed keys (to clean links leading to this keys)
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
}
