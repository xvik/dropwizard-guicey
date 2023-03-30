package ru.vyarus.dropwizard.guice.examples.service;

import javax.inject.Singleton;

/**
 * @author Vyacheslav Rusakov
 * @since 27.07.2017
 */
@Singleton
public class SampleService {

    // state used to show that service instance is the same for guice and sub-resource
    private String state;

    public void setState(String state) {
        this.state = state;
    }

    public String applyState(String id) {
        return state + " " + id;
    }
}
