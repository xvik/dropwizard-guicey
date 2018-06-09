package ru.vyarus.dropwizard.guice.yaml.support

import io.dropwizard.Configuration

/**
 * @author Vyacheslav Rusakov
 * @since 06.06.2018
 */
class NotUniqueSubConfig extends Configuration {

    SubConfig<String> sub1
    SubConfig<String> sub2
    SubConfig<Integer> sub3

    static class SubConfig<T> {
        String sub;
    }
}
