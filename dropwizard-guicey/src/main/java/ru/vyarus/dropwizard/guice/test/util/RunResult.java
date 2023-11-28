package ru.vyarus.dropwizard.guice.test.util;

import com.google.inject.Injector;
import com.google.inject.Key;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.DropwizardTestSupport;
import ru.vyarus.dropwizard.guice.test.GuiceyTestSupport;


/**
 * Application run result object for {@link ru.vyarus.dropwizard.guice.test.TestSupport} runs. It is important
 * to construct this object in time of running application because it would be impossible to reference these
 * objects after application shutdown.
 * <p>
 * Object supposed to be used for assertions after the application stopped.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 24.11.2023
 */
public class RunResult<C extends Configuration> {
    private final DropwizardTestSupport<C> support;
    private final Injector injector;

    public RunResult(final DropwizardTestSupport<C> support, final Injector injector) {
        this.support = support;
        this.injector = injector;
    }

    /**
     * @return support object used for application run
     */
    public DropwizardTestSupport<C> getSupport() {
        return support;
    }

    /**
     * @return injector, created during application run
     */
    public Injector getInjector() {
        return injector;
    }

    /**
     * @return application instance (used for run)
     */
    public Application<C> getApplication() {
        return support.getApplication();
    }

    /**
     * @return environment instance (used for run)
     */
    public Environment getEnvironment() {
        return support.getEnvironment();
    }

    /**
     * @return configuration instance
     */
    public C getConfiguration() {
        return support.getConfiguration();
    }

    /**
     * Access guice bean.
     *
     * @param type bean type
     * @param <T>  target type
     * @return bean instance or null
     */
    public <T> T getBean(final Class<T> type) {
        return getBean(Key.get(type));
    }

    /**
     * Access guice bean by mapping key (for qualified or generified bindings).
     *
     * @param key bean key
     * @param <T> target type
     * @return bean instance
     */
    public <T> T getBean(final Key<T> key) {
        return injector.getInstance(key);
    }

    /**
     * @return true for full web app run, false for core run (guice injector only)
     */
    public boolean isWebRun() {
        return !(support instanceof GuiceyTestSupport);
    }
}
