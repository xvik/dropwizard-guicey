package ru.vyarus.dropwizard.guice.diagnostic.support.module

import com.google.inject.AbstractModule

import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 11.10.2019
 */
class ModuleWithExtensions extends AbstractModule {

    @Override
    protected void configure() {
        bind(ModuleFeature)
    }

    @Path("/feature")
    static class ModuleFeature {}
}
