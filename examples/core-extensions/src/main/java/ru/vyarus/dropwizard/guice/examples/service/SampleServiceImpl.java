package ru.vyarus.dropwizard.guice.examples.service;

import javax.inject.Singleton;

/**
 * @author Vyacheslav Rusakov
 * @since 27.01.2016
 */
@Singleton
public class SampleServiceImpl implements SampleService {

    @Override
    public String foo() {
        return "foo";
    }
}
