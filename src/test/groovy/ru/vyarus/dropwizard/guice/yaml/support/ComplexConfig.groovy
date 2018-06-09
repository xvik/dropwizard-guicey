package ru.vyarus.dropwizard.guice.yaml.support

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import ru.vyarus.dropwizard.guice.yaml.support.ComplexConfig.Iface

/**
 * @author Vyacheslav Rusakov
 * @since 06.05.2018
 */
class ComplexConfig extends Configuration implements Iface{

    SubConfig sub

    @JsonProperty
    private Parametrized<Integer> one // field access

    static class SubConfig {
        String sub;
        Parametrized<String> two
    }

    static class Parametrized<T> {

        List<T> list;
    }

    interface Iface {

    }
}
