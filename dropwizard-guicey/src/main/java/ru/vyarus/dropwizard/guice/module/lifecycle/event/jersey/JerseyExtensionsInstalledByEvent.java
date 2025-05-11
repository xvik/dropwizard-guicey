package ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey;

import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.JerseyPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.InstallersResolvedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called when {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} installer installed all
 * related extensions and only for installers actually performed installations (extensions list never empty).
 * Provides installer and installed extensions types.
 * <p>
 * At this point hk is not completely started and so hk managed extensions
 * ({@link JerseyManaged}) couldn't be obtained yet
 * (even though you have access to root service locator). But extensions managed by guice could be obtained
 * from guice context.
 * <p>
 * Installer passed by type to simplify differentiation. If, for some reason, you need to access installer
 * instance then save installers in {@link InstallersResolvedEvent} and find by type
 * (only one installer of exact type could be registered).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class JerseyExtensionsInstalledByEvent extends JerseyPhaseEvent {

    private final Class<? extends FeatureInstaller> installer;
    private final List<Class<?>> installed;

    /**
     * Create event.
     *
     * @param context   event context
     * @param installer installer type
     * @param installed installed extensions
     */
    public JerseyExtensionsInstalledByEvent(final EventsContext context,
                                            final Class<? extends FeatureInstaller> installer,
                                            final List<Class<?>> installed) {
        super(GuiceyLifecycle.JerseyExtensionsInstalledBy, context);
        this.installer = installer;
        this.installed = installed;
    }

    /**
     * @return installer type
     */
    public Class<? extends FeatureInstaller> getInstaller() {
        return installer;
    }

    /**
     * @return installed extensions type
     */
    public List<Class<?>> getInstalled() {
        return installed;
    }
}
