package ru.vyarus.dropwizard.guice.examples.bundle.service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

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
