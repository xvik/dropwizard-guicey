package ru.vyarus.dropwizard.guice.yaml.support

import io.dropwizard.core.Configuration

/**
 * @author Vyacheslav Rusakov
 * @since 29.08.2019
 */
class RecursiveIndirectlyConfig extends Configuration {

    A next

    static class A {
        String value
        B next
    }

    static class B {
        String value
        A next
    }
}
