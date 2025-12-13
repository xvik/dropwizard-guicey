package ru.vyarus.dropwizard.guice.test.responsemodel.intercept;

import com.google.inject.AbstractModule;
import ru.vyarus.dropwizard.guice.test.responsemodel.ModelTracker;

/**
 * Guice module for resource model interceptor. Actual interceptor is {@link ResponseInterceptorFilter}, which
 * supposed to be recognized by guicey as extension.
 *
 * @author Vyacheslav Rusakov
 * @since 10.12.2025
 */
public class ResponseInterceptorModule extends AbstractModule {

    private final boolean interceptErrors;

    /**
     * Creates interceptor guice module.
     *
     * @param interceptErrors true to intercept error pages model
     */
    public ResponseInterceptorModule(final boolean interceptErrors) {
        this.interceptErrors = interceptErrors;
    }

    @Override
    protected void configure() {
        final ResponseInterceptorFilter filter = new ResponseInterceptorFilter(interceptErrors);
        bind(ResponseInterceptorFilter.class).toInstance(filter);

        final ModelTracker tracker = new ModelTracker(filter);
        bind(ModelTracker.class).toInstance(tracker);
    }
}
