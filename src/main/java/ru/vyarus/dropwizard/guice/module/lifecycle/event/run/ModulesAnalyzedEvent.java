package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import com.google.inject.Binding;
import com.google.inject.Module;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called when guice bindings analyzed and all extensions detected. Provides list of all recognized binding extensions
 * (including disabled), disabled transitive modules and extension bindings. Called only if bindings analysis is
 * enabled.
 * <p>
 * May be used for consultation only. Extension instances are not yet available (guice context is not created).
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2019
 */
public class ModulesAnalyzedEvent extends RunPhaseEvent {

    private final List<Module> analyzedModules;
    private final List<Class<?>> extensions;
    private final List<Class<? extends Module>> transitiveModulesRemoved;
    private final List<Binding> bindingsRemoved;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public ModulesAnalyzedEvent(final EventsContext context,
                                final List<Module> analyzedModules,
                                final List<Class<?>> extensions,
                                final List<Class<? extends Module>> transitiveModulesRemoved,
                                final List<Binding> bindingsRemoved) {
        super(GuiceyLifecycle.ModulesAnalyzed, context);
        this.analyzedModules = analyzedModules;
        this.extensions = extensions;
        this.transitiveModulesRemoved = transitiveModulesRemoved;
        this.bindingsRemoved = bindingsRemoved;
    }

    /**
     * @return list of module instances used for analysis or empty list
     */
    public List<Module> getAnalyzedModules() {
        return analyzedModules;
    }

    /**
     * @return all registered manual extensions (including possibly disabled in the future)
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }

    /**
     * Guice disables mechanism may directly remove upper level modules, but to to remove modules, installed
     * by top level modules bindings analysis is required. This method returns only removed inner modules.
     * It does not provide information of exact removed modules: multiple module instances may be registered
     * with the same type and all of them will be removed.
     *
     * @return removed inner guice modules (after bindings analysis) or empty list
     */
    public List<Class<? extends Module>> getTransitiveModulesRemoved() {
        return transitiveModulesRemoved;
    }

    /**
     * If extension is declared manually in guice module and extension is disabled then binding directly removed
     * under bindings analysis. Returned list exactly represent disabled extension bindings (removed). Note that
     * disabled inner modules remove is essentially the same (removing all bindings sources in disabled module),
     * but such bindings are not appear in this list (there could be too many of them and generally not interesting
     * information).
     *
     * @return disabled extensions bindings removed or empty list
     */
    public List<Binding> getBindingsRemoved() {
        return bindingsRemoved;
    }
}
