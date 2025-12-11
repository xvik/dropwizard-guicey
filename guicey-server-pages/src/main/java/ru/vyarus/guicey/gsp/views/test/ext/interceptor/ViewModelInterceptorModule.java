package ru.vyarus.guicey.gsp.views.test.ext.interceptor;

import com.google.inject.AbstractModule;
import ru.vyarus.guicey.gsp.views.test.ext.ViewModelTracker;

/**
 * Guice module for view model interceptor.
 *
 * @author Vyacheslav Rusakov
 * @since 10.12.2025
 */
public class ViewModelInterceptorModule extends AbstractModule {

    private final boolean interceptErrors;

    /**
     * Creates interceptor guice module.
     *
     * @param interceptErrors true to intercept error pages model
     */
    public ViewModelInterceptorModule(final boolean interceptErrors) {
        this.interceptErrors = interceptErrors;
    }

    @Override
    protected void configure() {
        final ViewModelFilter filter = new ViewModelFilter(interceptErrors);
        bind(ViewModelFilter.class).toInstance(filter);

        final ViewModelTracker tracker = new ViewModelTracker(filter);
        bind(ViewModelTracker.class).toInstance(tracker);
    }
}
