package ru.vyarus.dropwizard.guice.yaml.support

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.core.Configuration

/**
 * @author Vyacheslav Rusakov
 * @since 07.05.2018
 */
class ComplexGenericCase extends Configuration {

    @JsonProperty
    Sub<String> sub

    interface Sub<T> {
        T getSmth()
    }

    static class SubImpl<K> implements Sub<K> {
        @Override
        @JsonProperty
        K getSmth() {
            "sample"
        }

        @JsonProperty
        K getVal() {
            null
        }
    }
}
