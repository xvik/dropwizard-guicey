package ru.vyarus.dropwizard.guice.support.util

import com.google.inject.AbstractModule

/**
 * Module used to explicitly bind services normally resolved by JIT.
 *
 * @author Vyacheslav Rusakov
 * @since 19.06.2016
 */
class BindModule extends AbstractModule {
    private Class[] types

    BindModule(Class... types) {
        this.types = types
    }

    @Override
    protected void configure() {
        types.each {
            bind(it)
        }
    }
}
