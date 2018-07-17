package ru.vyarus.dropwizard.guice.yaml.support

import io.dropwizard.Configuration

/**
 * @author Vyacheslav Rusakov
 * @since 17.07.2018
 */
class FailedGetterConfig extends Configuration {

    Sub sub = new Sub()

    String getSample() {
        throw new IllegalStateException("Can't get")
    }

    static class Sub {
        String getSample() {
            throw new IllegalStateException("Can't get")
        }
    }
}
