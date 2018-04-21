package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import com.google.inject.Module;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;

import java.util.List;

/**
 * Called just before guice injector creation. Provides all configured modules (main and override).
 * Called even if no modules were used at all (to indicate major lifecycle point).
 * <p>
 * Modules may be post-processed here (e.g. some special marker interface support may be implemented here).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class InjectorCreationEvent extends RunPhaseEvent {

    private final List<Module> modules;
    private final List<Module> overriding;

    public InjectorCreationEvent(final OptionsInfo options,
                                 final Bootstrap bootstrap,
                                 final Configuration configuration,
                                 final Environment environment,
                                 final List<Module> modules,
                                 final List<Module> overriding) {
        super(GuiceyLifecycle.InjectorCreation, options, bootstrap, configuration, environment);
        this.modules = modules;
        this.overriding = overriding;
    }

    /**
     * @return list of all enabled guice modules or empty list if no modules registered or all of them were disabled
     */
    public List<Module> getModules() {
        return modules;
    }

    /**
     * @return list of all overriding guice modules or empty list if not overriding modules registered or all of them
     * were disabled
     */
    public List<Module> getOverridingModules() {
        return overriding;
    }
}
