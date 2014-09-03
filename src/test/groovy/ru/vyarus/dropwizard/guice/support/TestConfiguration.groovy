package ru.vyarus.dropwizard.guice.support

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration

/**
 * @author Vyacheslav Rusakov 
 * @since 01.09.2014
 */
class TestConfiguration extends Configuration {

    @JsonProperty
    int foo
}
