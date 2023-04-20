package ru.vyarus.dropwizard.guice.yaml.support

import com.fasterxml.jackson.annotation.JsonIgnore
import io.dropwizard.Configuration

/**
 * @author Vyacheslav Rusakov
 * @since 17.07.2018
 */
class IgnorePathConfig extends Configuration {

    String foo
    private String prop

    // no binding
    @JsonIgnore
    String getProp() {
        return prop
    }

    void setProp(String prop) {
        this.prop = prop
    }
}
