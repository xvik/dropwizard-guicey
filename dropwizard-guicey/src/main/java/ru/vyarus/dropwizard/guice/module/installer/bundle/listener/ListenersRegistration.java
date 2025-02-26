package ru.vyarus.dropwizard.guice.module.installer.bundle.listener;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import org.eclipse.jetty.util.component.LifeCycle;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener;

import java.util.function.Consumer;

/**
 * Unified listeners registration logic, common for {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder},
 * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap} and
 * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment}.
 * <p>
 * Provides simple shortcuts for the most useful events.
 *
 * @author Vyacheslav Rusakov
 * @since 24.02.2025
 * @param <T> actual builder type
 */
public abstract class ListenersRegistration<T> {

    private final ConfigurationContext context;

    public ListenersRegistration(final ConfigurationContext context) {
        this.context = context;
    }

    /**
     * Guicey broadcast a lot of events in order to indicate lifecycle phases
     * ({@link ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle}). This could be useful
     * for diagnostic logging (like {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#printLifecyclePhases()}) or
     * to implement special behaviours on installers, bundles, modules extensions (listeners have access to everything).
     * For example, {@link ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule} like support for guice
     * modules could be implemented with listeners.
     * <p>
     * Configuration items (modules, extensions, bundles) are not aware of each other and listeners
     * could be used to tie them. For example, to tell bundle if some other bundles registered (limited
     * applicability, but just for example).
     * <p>
     * You can also use {@link ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter} when you need to
     * handle multiple events (it replaces direct events handling with simple methods).
     * <p>
     * Listener is not registered if equal listener were already registered ({@link java.util.Set} used as
     * listeners storage), so if you need to be sure that only one instance of some listener will be used
     * implement {@link Object#equals(Object)} and {@link Object#hashCode()}.
     *
     * @param listeners guicey lifecycle listeners
     * @return builder instance for chained calls
     * @see ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle
     * @see ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter
     * @see ru.vyarus.dropwizard.guice.module.lifecycle.UniqueGuiceyLifecycleListener
     */
    public T listen(final GuiceyLifecycleListener... listeners) {
        context.lifecycle().register(listeners);
        return self();
    }

    /**
     * Code to execute after guice injector creation (but still under run phase). May be used for manual
     * configurations (registrations into dropwizard environment).
     * <p>
     * Listener will be called on environment command start too.
     * <p>
     * Note: there is no registration method for this listener in main guice bundle builder
     * ({@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder}) because it is assumed, that such blocks would
     * always be wrapped with bundles to improve application readability.
     *
     * @param listener listener to call after injector creation
     * @param <C> configuration type
     * @return builder instance for chained calls
     */
    public <C extends Configuration> T onGuiceyStartup(final GuiceyStartupListener<C> listener) {
        return listen(new GuiceyStartupListenerAdapter<C>(listener));
    }

    /**
     * Code to execute after complete application startup. For server command it would happen after jetty startup
     * and for lightweight guicey test helpers ({@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp}) - after
     * guicey start (as jetty not started in this case). In both cases, application completely started at this moment.
     * Suitable for reporting.
     * <p>
     * If you need to listen only for real server startup then use
     * {@link #listenServer(io.dropwizard.lifecycle.ServerLifecycleListener)} instead.
     * <p>
     * Not called on custom command execution (because no lifecycle involved in this case). In this case you can use
     * {@link #onGuiceyStartup(GuiceyStartupListener)} as always executed point.
     *
     * @param listener listener to call on server startup
     * @return builder instance for chained calls
     */
    public T onApplicationStartup(final ApplicationStartupListener listener) {
        return listen(new ApplicationStartupListenerAdapter(listener));
    }

    /**
     * Code to execute after complete application shutdown. Called not only for real application but for
     * environment commands and lightweight guicey test helpers
     * ({@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp}). Suitable for closing additional resources.
     * <p>
     * If you need to listen only for real server shutdown then use
     * {@link #listenServer(io.dropwizard.lifecycle.ServerLifecycleListener)} instead.
     * <p>
     * Not called on command execution because no lifecycle involved in this case.
     *
     * @param listener listener to call on server startup
     * @return builder instance for chained calls
     */
    public T onApplicationShutdown(final ApplicationShutdownListener listener) {
        return listen(new ApplicationShutdownListenerAdapter(listener));
    }

    /**
     * Shortcut for {@link io.dropwizard.lifecycle.ServerLifecycleListener} registration.
     * <p>
     * Note that server listener is called only when jetty starts up and so will not be called with lightweight
     * guicey test helpers {@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp}. Prefer using
     * {@link #onApplicationStartup(ApplicationStartupListener)} to be correctly called in tests (of course, if not
     * server only execution is desired).
     * <p>
     * Obviously not called for custom command execution.
     *
     * @param listener server startup listener.
     * @return builder instance for chained calls
     */
    public T listenServer(final ServerLifecycleListener listener) {
        withEnvironment(environment -> environment.lifecycle().addServerLifecycleListener(listener));
        return self();
    }

    /**
     * Shortcut for jetty lifecycle listener {@link org.eclipse.jetty.util.component.LifeCycle.Listener listener}
     * registration.
     * <p>
     * Lifecycle listeners are called with lightweight guicey test helpers
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp} which makes them perfectly suitable for reporting.
     * <p>
     * If only startup event is required, prefer {@link #onApplicationStartup(ApplicationStartupListener)} method
     * as more expressive and easier to use.
     * <p>
     * Listeners are not called on custom command execution.
     *
     * @param listener jetty
     * @return builder instance for chained calls
     */
    public T listenJetty(final LifeCycle.Listener listener) {
        withEnvironment(environment -> environment.lifecycle().addEventListener(listener));
        return self();
    }

    protected abstract void withEnvironment(Consumer<Environment> action);

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }
}
