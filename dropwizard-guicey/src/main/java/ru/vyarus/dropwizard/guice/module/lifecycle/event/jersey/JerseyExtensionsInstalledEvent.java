package ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey;

import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.JerseyPhaseEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.internal.EventsContext;

import java.util.List;

/**
 * Called after all {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} installers install
 * related extensions and only when at least one extension was installed. Provides list of all used (enabled)
 * extensions.
 * <p>
 * At this point hk is not completely started and so hk managed extensions
 * ({@link JerseyManaged}) couldn't be obtained yet
 * (even though you have access to root service locator). But extensions managed by guice could be obtained
 * from guice context.
 * <p>
 * To listen hk lifecycle further use jersey events (like in
 * {@link ru.vyarus.dropwizard.guice.debug.LifecycleDiagnostic}).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class JerseyExtensionsInstalledEvent extends JerseyPhaseEvent {

    private final List<Class<?>> extensions;

    /**
     * Create event.
     *
     * @param context    event context
     * @param extensions installed extensions
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public JerseyExtensionsInstalledEvent(final EventsContext context,
                                          final List<Class<?>> extensions) {
        super(GuiceyLifecycle.JerseyExtensionsInstalled, context);
        this.extensions = extensions;
    }

    /**
     * @return list of all installed extensions
     */
    public List<Class<?>> getExtensions() {
        return extensions;
    }
}
