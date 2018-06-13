package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.support.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class for guice modules processing.
 *
 * @author Vyacheslav Rusakov
 * @since 25.04.2018
 */
public final class ModulesSupport {

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
        final List<Module> normalModules = context.getNormalModules();
        final List<Module> overridingModules = context.getOverridingModules();
        // use different lists to avoid possible side effects from listeners (not allowed to exclude or modify order)
        context.lifecycle().injectorCreation(
                new ArrayList<>(normalModules),
                new ArrayList<>(overridingModules),
                context.getDisabledModules());
        return overridingModules.isEmpty() ? normalModules
                : Collections.singletonList(Modules.override(normalModules).with(overridingModules));
    }
}
