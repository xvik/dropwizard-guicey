package ru.vyarus.dropwizard.guice.examples;

import io.dropwizard.core.Configuration;

/**
 * @author Vyacheslav Rusakov
 * @since 07.05.2025
 */
public class ExampleConfig extends Configuration {

    private String petStoreUrl;
    private boolean startFakeStore;

    public String getPetStoreUrl() {
        return petStoreUrl;
    }

    public boolean isStartFakeStore() {
        return startFakeStore;
    }
}
