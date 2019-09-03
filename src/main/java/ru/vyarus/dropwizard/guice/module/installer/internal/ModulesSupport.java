package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.common.base.Stopwatch;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
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

import static ru.vyarus.dropwizard.guice.GuiceyOptions.ConfigureFromGuiceModules;
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
        final Boolean configureFromGuice = context.option(ConfigureFromGuiceModules);
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
                analyzeAndFilterBindings(context, elements);
                modulesTimer.start();

                // wrap raw elements into module to avoid duplicate work on guice startup and put back bootstrap
                modules = Arrays.asList(Elements.getModule(elements), bootstrap);
            } catch (Exception ex) {
                // better show meaningful message then just fail entire startup with ambiguous message
                // NOTE if guice configuration is not OK it will fail here too, but user will see injector creation
                // error as last error in logs.
                LOGGER.error("Failed to analyze guice bindings - skipping this step. Note that configuration"
                        + " from bindings may be switched off with " + GuiceyOptions.class.getSimpleName() + "."
                        + ConfigureFromGuiceModules.name() + " option.", ex);
                // recover and use original modules
                modules.add(bootstrap);
                if (!modulesTimer.isRunning()) {
                    modulesTimer.start();
                }
            }
        }
        return modules;
    }

    private static void analyzeAndFilterBindings(final ConfigurationContext context, final List<Element> elements) {
        final Stopwatch itimer = context.stat().timer(InstallersTime);
        final Stopwatch timer = context.stat().timer(Stat.ExtensionsRecognitionTime);
        final Iterator<Element> it = elements.iterator();
        final List<Class<?>> extensions = new ArrayList<>();
        while (it.hasNext()) {
            final Element element = it.next();
            // filter constants, listeners, aop etc.
            if (element instanceof Binding) {
                context.stat().count(Stat.BindingsCount, 1);
                final Key key = ((Binding) element).getKey();
                // extensions bindings may be only unqualified, class only (no generified types)
                if (key.getAnnotation() == null && key.getTypeLiteral().getType() instanceof Class) {
                    context.stat().count(Stat.AnalyzedBindingsCount, 1);
                    final Class type = key.getTypeLiteral().getRawType();
                    if (ExtensionsSupport.registerExtensionBinding(context, type,
                            (Binding<?>) element, BindingUtils.getTopDeclarationModule(element))) {
                        LOGGER.debug("Extension detected from guice binding: {}", type.getSimpleName());
                        extensions.add(type);
                        if (!context.isExtensionEnabled(type)) {
                            it.remove();
                            LOGGER.debug("Removed disabled extension binding: {}", type.getSimpleName());
                            context.stat().count(Stat.RemovedBindingsCount, 1);
                        }
                    }
                }
            }
        }
        context.lifecycle().bindingExtensionsResolved(extensions);
        timer.stop();
        itimer.stop();
    }
}
