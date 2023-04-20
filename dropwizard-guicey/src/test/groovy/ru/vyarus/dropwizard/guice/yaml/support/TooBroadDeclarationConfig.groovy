package ru.vyarus.dropwizard.guice.yaml.support

import io.dropwizard.Configuration

/**
 * @author Vyacheslav Rusakov
 * @since 07.06.2018
 */
class TooBroadDeclarationConfig extends Configuration {

    ArrayList<String> foo
    ExtraList<String, Integer> bar


    static class ExtraList<T, K> extends ArrayList<K> {}
}
