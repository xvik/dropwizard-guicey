package ru.vyarus.dropwizard.guice.examples;

import io.dropwizard.Configuration;
import ru.vyarus.dropwizard.guice.examples.modules.Some3rdPatyModule;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;

/**
 * @author Vyacheslav Rusakov
 * @since 12.02.2017
 */
public class SampleModule extends DropwizardAwareModule<Configuration> {

    @Override
    protected void configure() {
        // 3rd party guice modules installation
        install(new Some3rdPatyModule());

        // example access to dropwizard objects from module
        configuration();
        environment();
        bootstrap();
    }
}
