package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import com.google.inject.Module;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called just before guice injector creation. Provides all configured modules (main and override) and
 * all disabled modules. Called even if no modules were used at all (to indicate major lifecycle point).
 * <p>
 * Modules may be post-processed here (e.g. some special marker interface support may be implemented here).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class InjectorCreationEvent extends RunPhaseEvent {

    private final List<Module> modules;
    private final List<Module> overriding;
    private final List<Module> disabled;
    private final List<Module> ignored;

    public InjectorCreationEvent(final EventsContext context,
                                 final List<Module> modules,
                                 final List<Module> overriding,
                                 final List<Module> disabled,
                                 final List<Module> ignored) {
        super(GuiceyLifecycle.InjectorCreation, context);
        this.modules = modules;
        this.overriding = overriding;
        this.disabled = disabled;
        this.ignored = ignored;
    }

    /**
     * NOTE: if bindings analysis is enabled
     * ({@link ru.vyarus.dropwizard.guice.GuiceyOptions#AnalyzeGuiceModules}) then these modules were already
     * processed and repackaged (in order to remove disabled inner modules and extensions). This list is useful for
     * information only and any modifications to module instances makes no sense.
     *
     * @return list of all enabled guice modules or empty list if no modules registered or all of them were disabled
     */
    public List<Module> getModules() {
        return modules;
    }

    /**
     * In contrast to normal modules, which are repackaged during bindings analysis, overriding modules are always
     * used as is and so instances may be modified (modules configuration called after this event).
     *
     * @return list of all overriding guice modules or empty list if not overriding modules registered or all of them
     * were disabled
     */
    public List<Module> getOverridingModules() {
        return overriding;
    }

    /**
     * @return list of all disabled modules or empty list
     */
    public List<Module> getDisabled() {
        return disabled;
    }

    /**
     * @return list of all ignored modules (duplicates) or empty list
     */
    public List<Module> getIgnored() {
        return ignored;
    }
}
