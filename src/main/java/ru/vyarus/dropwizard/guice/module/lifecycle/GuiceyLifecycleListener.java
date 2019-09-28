package ru.vyarus.dropwizard.guice.module.lifecycle;

import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent;

/**
 * Guicey lifecycle listener covers all valuable phases of guicey configuration. It could be used either for
 * startup monitoring or for some advanced features implementation (based on installers, extensions modules or bundles
 * post-processing).
 * <p>
 * Example usage: {@link ru.vyarus.dropwizard.guice.debug.LifecycleDiagnostic}.
 * <p>
 * Listener is not registered if equal listener were already registered ({@link java.util.Set} used as
 * listeners storage), so if you need to be sure that only one instance of some listener will be used
 * implement {@link Object#equals(Object)} and {@link Object#hashCode()}. For example, this is used to resolve case
 * where {@link ru.vyarus.dropwizard.guice.debug.hook.DiagnosticHook} installed and some reports were already enabled
 * in bundle directly: thanks to correct equals in reports, user will not see duplicate reports.
 *
 * @author Vyacheslav Rusakov
 * @see GuiceyLifecycleAdapter
 * @since 17.04.2018
 */
@FunctionalInterface
public interface GuiceyLifecycleListener {

    /**
     * Called with specific lifecycle event. Event object may contain event specific objects. Event always
     * contain main objects, available at this point (like configuration, environment, injector etc).
     *
     * @param event event instance
     * @see GuiceyLifecycle for possible event types
     */
    void onEvent(GuiceyLifecycleEvent event);
}
