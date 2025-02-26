package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest.support;

import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2025
 */
public class RestStubApp extends DefaultTestApp {
    @Override
    protected GuiceBundle configure() {
        return GuiceBundle.builder()
                .enableAutoConfig()
                .build();
    }
}
