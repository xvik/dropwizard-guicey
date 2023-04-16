package ru.vyarus.dropwizard.guice.examples.bundle.service;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * @author Vyacheslav Rusakov
 * @since 31.12.2019
 */
@Singleton
public class SampleService {

    @Inject @Named("bundle.config")
    private String config;

    public String getConfig() {
        return config;
    }
}
