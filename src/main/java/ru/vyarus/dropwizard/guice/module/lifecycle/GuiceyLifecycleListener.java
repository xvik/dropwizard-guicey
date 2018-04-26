package ru.vyarus.dropwizard.guice.module.lifecycle;

import ru.vyarus.dropwizard.guice.module.lifecycle.event.GuiceyLifecycleEvent;

/**
 * Guicey lifecycle listener covers all valuable phases of guicey configuration. It could be used either for
 * startup monitoring or for some advanced features implementation (based on installers, extensions modules or bundles
 * post-processing).
 * <p>
 * Example usage: {@link ru.vyarus.dropwizard.guice.module.lifecycle.debug.DebugGuiceyLifecycle}.
 * <p>
 * Listener could also implement {@link ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator}
 * in order to apply special configurations. For example, this could be some diagnostic extensions.
 * There is no other way to modify configuration in listener.
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
