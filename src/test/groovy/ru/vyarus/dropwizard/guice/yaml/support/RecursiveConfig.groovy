package ru.vyarus.dropwizard.guice.yaml.support


import io.dropwizard.core.Configuration

/**
 * @author Vyacheslav Rusakov
 * @since 26.08.2019
 */
class RecursiveConfig extends Configuration {

    CustomProperty customProperty


    static class CustomProperty {
        String value
        CustomProperty customProperty
    }
}
