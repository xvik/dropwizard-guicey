package ru.vyarus.dropwizard.guice.examples.service;

import jakarta.inject.Singleton;

/**
 * @author Vyacheslav Rusakov
 * @since 31.12.2019
 */
@Singleton
public class SampleService {

    public String servletPart() {
        return "srvlt";
    }

    public String filterPart() {
        return "fltr";
    }

    public String listenerPart() {
        return "listen!";
    }
}
