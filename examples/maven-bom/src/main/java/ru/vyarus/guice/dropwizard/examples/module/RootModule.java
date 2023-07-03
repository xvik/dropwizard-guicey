package ru.vyarus.guice.dropwizard.examples.module;

import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;
import ru.vyarus.guice.dropwizard.examples.SampleConfiguration;

/**
 * @author Vyacheslav Rusakov
 * @since 03.07.2023
 */
public class RootModule extends DropwizardAwareModule<SampleConfiguration> {

    @Override
    protected void configure() {
        // 3rd party guice modules installation
        install(new Some3rdPartyModule());

        // example access to dropwizard objects from module
        configuration();
        environment();
        bootstrap();
    }
}
