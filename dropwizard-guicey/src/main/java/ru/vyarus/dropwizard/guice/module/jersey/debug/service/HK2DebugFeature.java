package ru.vyarus.dropwizard.guice.module.jersey.debug.service;

import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Jersey feature registers services instantiation tracker.
 *
 * @author Vyacheslav Rusakov
 * @since 15.01.2016
 */
@Singleton
public class HK2DebugFeature implements Feature {

    private final HK2InstanceListener listener;

    @Inject
    public HK2DebugFeature(final HK2InstanceListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(listener).to(InstanceLifecycleListener.class);
            }
        });
        return true;
    }
}
