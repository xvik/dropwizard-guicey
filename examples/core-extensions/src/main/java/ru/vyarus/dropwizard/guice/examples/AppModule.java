package ru.vyarus.dropwizard.guice.examples;

import com.google.inject.AbstractModule;
import ru.vyarus.dropwizard.guice.examples.rest.MyResourceFromBinding;
import ru.vyarus.dropwizard.guice.examples.service.SampleService;
import ru.vyarus.dropwizard.guice.examples.service.SampleServiceImpl;

/**
 * @author Vyacheslav Rusakov
 * @since 27.01.2016
 */
public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SampleService.class).to(SampleServiceImpl.class);

        // extension recognition from guice binding
        bind(MyResourceFromBinding.class);
    }
}
