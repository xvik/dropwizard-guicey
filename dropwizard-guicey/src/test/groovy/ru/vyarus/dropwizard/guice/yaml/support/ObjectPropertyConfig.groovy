package ru.vyarus.dropwizard.guice.yaml.support

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration

/**
 * @author Vyacheslav Rusakov
 * @since 05.06.2018
 */
class ObjectPropertyConfig extends Configuration {

    @JsonProperty
    Object sub
}
