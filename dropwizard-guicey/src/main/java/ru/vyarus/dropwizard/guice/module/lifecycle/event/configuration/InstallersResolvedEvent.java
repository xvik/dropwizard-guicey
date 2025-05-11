package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.ConfigurationPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called after all installers resolved (including installers found with classpath scan) and prepared for
 * processing extensions. Provides list of all enabled installers. Called even if no installers are used.
 * <p>
 * May be used to post-process installers (e.g. add some conventional configuration by some marker interface).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class InstallersResolvedEvent extends ConfigurationPhaseEvent {
    private final List<FeatureInstaller> installers;
    private final List<Class<? extends FeatureInstaller>> disabled;

    /**
     * Create event.
     *
     * @param context    event context
     * @param installers installers
     * @param disabled   disabled installers
     */
    public InstallersResolvedEvent(final EventsContext context,
                                   final List<FeatureInstaller> installers,
                                   final List<Class<? extends FeatureInstaller>> disabled) {
        super(GuiceyLifecycle.InstallersResolved, context);
        this.installers = installers;
        this.disabled = disabled;
    }

    /**
     * @return list of all enabled installers or empty list if no installers registered or all of them were
     * disabled
     */
    public List<FeatureInstaller> getInstallers() {
        return installers;
    }

    /**
     * Disabled installers are never instantiated and so instances are not available.
     *
     * @return list of disabled installer types
     */
    public List<Class<? extends FeatureInstaller>> getDisabled() {
        return disabled;
    }
}
