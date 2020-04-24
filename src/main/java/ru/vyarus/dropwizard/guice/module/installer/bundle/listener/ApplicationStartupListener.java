package ru.vyarus.dropwizard.guice.module.installer.bundle.listener;

import com.google.inject.Injector;
import io.dropwizard.lifecycle.ServerLifecycleListener;

/**
 * Dropwizard application complete startup listener. Useful for delayed code execution after server startup.
 * Supposed to be used instead of {@link ServerLifecycleListener} (because server listener is not called for guicey
 * lightweight tests) and instead of {@link org.eclipse.jetty.util.component.LifeCycle.Listener} in cases when only
 * startup event is important (easier to use).
 * <p>
 * It also receives application injector to simplify usage.
 *
 * @author Vyacheslav Rusakov
 * @since 28.09.2019
 */
@FunctionalInterface
public interface ApplicationStartupListener {

    /**
     * Called after server startup or after guicey initialization in guicey-only tests.
     * <p>
     * Any thrown exception would shutdown startup.
     *
     * @param injector gucie injector
     * @throws Exception in case of errors
     */
    void started(Injector injector) throws Exception;
}
