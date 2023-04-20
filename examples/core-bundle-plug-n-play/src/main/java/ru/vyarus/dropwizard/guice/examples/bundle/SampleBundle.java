package ru.vyarus.dropwizard.guice.examples.bundle;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;

/**
 * To be able to override default bundle registration, bundle must be unique. In this case user will be able
 * to re-configure it by simply applying bundle directly.
 *
 * @author Vyacheslav Rusakov
 * @since 29.01.2016
 */
public class SampleBundle extends UniqueGuiceyBundle {

    private String config;

    public SampleBundle() {
        this("default");
    }

    public SampleBundle(String config) {
        this.config = config;
    }

    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        bootstrap
                .modules(new AbstractModule() {
                    @Override
                    protected void configure() {
                        // using constant binding to bypass bundle value
                        bind(String.class).annotatedWith(Names.named("bundle.config")).toInstance(config);
                    }
                });
    }
}
