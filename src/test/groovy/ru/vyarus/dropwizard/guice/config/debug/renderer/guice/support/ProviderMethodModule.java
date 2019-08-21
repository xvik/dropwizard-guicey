package ru.vyarus.dropwizard.guice.config.debug.renderer.guice.support;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2019
 */
public class ProviderMethodModule extends AbstractModule {

    @Override
    protected void configure() {
        
    }

    @Provides
    public Sample provide() {
        return new Sample();
    }


    public static class Sample {}
}
