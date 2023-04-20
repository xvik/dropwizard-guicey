package ru.vyarus.dropwizard.guice.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

/**
 * Groovy class can't be used anymore, because jackson 2.5 is very sensible for additional methods.
 *
 * @author Vyacheslav Rusakov 
 * @since 01.09.2014
 */
public class TestConfiguration extends Configuration {

    @JsonProperty
    public int foo;

    @JsonProperty
    public int bar;

    @JsonProperty
    public int baa;
}
