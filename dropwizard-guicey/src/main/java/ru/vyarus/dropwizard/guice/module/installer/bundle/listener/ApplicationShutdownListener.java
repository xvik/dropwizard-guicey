package ru.vyarus.dropwizard.guice.module.installer.bundle.listener;

import com.google.inject.Injector;

/**
 * Dropwizard application shut down listener. Useful for an additional shutdown logic. Supposed to be used instead of
 * {@link io.dropwizard.lifecycle.ServerLifecycleListener} (because server listener is not called for guicey
 * lightweight tests) and instead of {@link org.eclipse.jetty.util.component.LifeCycle.Listener} in cases when only
 * shutdown event is important (easier to use).
 *
 * @author Vyacheslav Rusakov
 * @since 24.02.2025
 */
@FunctionalInterface
public interface ApplicationShutdownListener {

    /**
     * Called after server shutdown (including shutdown after lightweight guicey tests).
     * <p>
     * Only an injector is provided because all other objects could be obtained from it, if required.
     *
     * @param injector guice injector
     */
    void stopped(Injector injector);
}
